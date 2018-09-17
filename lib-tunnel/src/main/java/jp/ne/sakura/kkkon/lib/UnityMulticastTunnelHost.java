package jp.ne.sakura.kkkon.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by kkkon on 2017/11/11.
 */

public class UnityMulticastTunnelHost
{
    private static final String TAG = "UnityMulticastTunnelHost";

    protected static Socket mSocket = null;
    protected static boolean mIsConnected = false;

    public static void init()
    {
        mIsConnected = false;

        Socket socket = null;
        SocketAddress addr = null;

        assert null == mSocket;
        try
        {
            addr = new InetSocketAddress("127.0.0.1", UnityPlayerConst.PLAYER_MULTICAST_PORT );
            socket = new Socket();
            socket.setSoTimeout( 30*1000 );

            socket.connect( addr, 30*1000 );

            mSocket = socket;
        }
        catch ( SocketException e )
        {
            DebugLog.e( TAG, "", e );
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "", e );
        }
    }

    public static void term()
    {
        if ( null != mSocket )
        {
            try { mSocket.close(); } catch ( Exception e ) {}
            mSocket = null;
        }
        mIsConnected = false;
    }

    public static UnityPlayerInfo poll()
    {
        if ( null == mSocket )
        {
            init();
        }

        if ( null == mSocket )
        {
            return null;
        }

        InputStream in = null;
        UnityPlayerInfo info = null;

        try
        {
            in = mSocket.getInputStream();
            byte[] buff = new byte[1024];
            final int readed = in.read( buff );

            if ( false == mIsConnected )
            {
                mIsConnected = true;
                DebugLog.e( TAG, "Connected to Target" );
            }

            if ( 0 < readed )
            {
                final String line = new String(buff, 0, readed);
                info = UnityPlayerInfo.parse(line);
            }
        }
        catch ( SocketTimeoutException e )
        {
            //DebugLog.d( TAG, "exception", e );
            if ( false == mIsConnected )
            {
                mIsConnected = true;
                DebugLog.v( TAG, "Connected to Target" );
            }
        }
        catch ( IOException e )
        {
            term();
        }

        return info;
    }

    public static void relay( final UnityPlayerInfo info )
    {
        InetAddress group = null;
        try
        {
            group = InetAddress.getByName( UnityPlayerConst.PLAYER_MULTICAST_GROUP );
        }
        catch ( UnknownHostException e )
        {
            DebugLog.e( TAG, "", e );
        }

        SocketAddress socketAddr = null;
        socketAddr = new InetSocketAddress( UnityPlayerConst.PLAYER_MULTICAST_GROUP, UnityPlayerConst.PLAYER_MULTICAST_PORT );
        //DebugLog.d( TAG, "socketAddr" + socketAddr );

        if ( null == group )
        {
            return;
        }

        {
            final String line = info.generateMessage();
            DebugLog.d( TAG, "line=" + line );
        }

        MulticastSocket socket = null;
        try
        {
            java.util.Enumeration<java.net.NetworkInterface> nis = java.net.NetworkInterface.getNetworkInterfaces();
            while ( nis.hasMoreElements() )
            {
                java.net.NetworkInterface ni = nis.nextElement();
                if ( !ni.isUp() )
                {
                    continue;
                }
                if ( ! ni.supportsMulticast() )
                {
                    continue;
                }

                if ( ni.isLoopback() )
                {
                    //continue;
                }

                try
                {
                    socket = new MulticastSocket( UnityPlayerConst.PLAYER_MULTICAST_PORT );
                    socket.setSoTimeout( 3 * 1000 );
                    socket.setNetworkInterface( ni );
                    socket.joinGroup( group );
                    //socket.joinGroup( socketAddr, ni ); // humm.. 'NoRouteToHostException' at send. need.SetNetworkInterface

                    final String line = info.generateMessage();
                    //DebugLog.d( TAG, "line=" + line );
                    final byte[] buff = line.getBytes();
                    //DatagramPacket packet = new DatagramPacket( buff, buff.length, group, UnityPlayerConst.PLAYER_MULTICAST_PORT );
                    DatagramPacket packet = new DatagramPacket( buff, buff.length, socketAddr );
                    socket.send( packet );

//                    DebugLog.d( "", ni.getDisplayName() );
//                    DebugLog.d( "", "  " );
//                    DebugLog.d( "", ni.getInterfaceAddresses().toString() );
                }
                catch ( SocketTimeoutException e )
                {
                    DebugLog.d( TAG, "", e );
                }
                catch ( IOException e )
                {
                    DebugLog.e( TAG, " ni=" + ni.getInterfaceAddresses().toString(), e );
                }
                finally
                {
                    if ( null != socket )
                    {
                        try { socket.leaveGroup( socketAddr, ni ); } catch ( Exception e ) {}
                        try { socket.close(); } catch ( Exception e ) {}
                    }
                }
            }
        }
        catch ( java.net.SocketException e )
        {

        }
    }
}
