package jp.ne.sakura.kkkon.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by kkkon on 2017/11/11.
 */

public class UnityMulticastTunnel
{
    private static final String TAG = "UnityMulticastTunnel";

    public static UnityPlayerInfo poll()
    {
        UnityPlayerInfo info = null;
        InetAddress group = null;
        try
        {
            group = InetAddress.getByName( UnityPlayerConst.PLAYER_MULTICAST_GROUP );
        }
        catch ( UnknownHostException e )
        {
            DebugLog.e( TAG, "", e );
        }
        /*
        SocketAddress group = null;
        group = new InetSocketAddress( UnityPlayerConst.PLAYER_MULTICAST_GROUP, UnityPlayerConst.PLAYER_MULTICAST_PORT );
        */

        if ( null == group )
        {
            return info;
        }

        MulticastSocket socket = null;
        try
        {
            socket = new MulticastSocket( UnityPlayerConst.PLAYER_MULTICAST_PORT );
            socket.setSoTimeout( 30 * 1000 );
            socket.joinGroup( group );

            byte[] buff = new byte[1024];

            DatagramPacket packet = new DatagramPacket( buff, buff.length );
            socket.receive( packet );

            if ( 0 < packet.getLength() )
            {
                final String line = new String(buff, 0, packet.getLength()-1);
                DebugLog.d( TAG, line );
                info = UnityPlayerInfo.parse( line );
            }

        }
        catch ( SocketTimeoutException e )
        {
            DebugLog.d( TAG, "", e );
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "", e );
        }
        finally
        {
            if ( null != socket )
            {
                try { socket.leaveGroup( group ); } catch ( Exception e ) {}
                try { socket.close(); } catch ( Exception e ) {}
            }
        }

        if ( null != info )
        {
            if ( ! info.mAllowDebug )
            {
                // drop UnityPlayer, not allow 'Script Debug'
                info = null;
            }
        }

        if ( null != info )
        {
            final int portDebug = info.getDebugPort();
            if ( ! availableMonoDebug( portDebug ) )
            {
                // drop UnityPlayer, not running mono debug
                info = null;
            }
        }

        return info;
    }

    public static boolean availableMonoDebug( final int portDebug )
    {
        // System.getProperty("os.name").toLower().indexOf("win") >= 0, File.separatorChar == '\\'
        File procNetTcp = new File( "/proc/net/tcp" );
        if ( ! procNetTcp.canRead() )
        {
            DebugLog.d( TAG, "not readable." + procNetTcp.getAbsolutePath() );
            return true;
        }

        boolean isRunningMonoDebug = false;
        BufferedReader br = null;
        try
        {
            br = new BufferedReader( new FileReader( procNetTcp ) );
            String line = null;
            while ( null != (line = br.readLine()) )
            {
                //DebugLog.d( TAG, line );
                /*
                {
                    final byte[] buff = line.getBytes();
                    StringBuffer sb = new StringBuffer();
                    for ( int index = 0; index < buff.length; ++index )
                    {
                        sb.append( String.format("%02x ", (buff[index]&0xff) ) );
                    }
                    DebugLog.d( TAG, sb.toString() );
                }
                */

                // sl,local_address,rem_address,st,tx_queue,rx_queue,tr,tm->when,retrnsmt,uid,timeout,inode
                final String[] params = line.trim().split("[\\s]+" );
                if ( null == params )
                {
                    continue;
                }
                if ( params.length < 3 )
                {
                    continue;
                }
                if ( params[1].indexOf(':') < 0 )
                {
                    continue;
                }
                if ( params[2].indexOf(':') < 0 )
                {
                    continue;
                }
                //DebugLog.d( TAG, params[1] + " " + params[2] );

                if ( ! "00000000:0000".equals(params[2]) )
                {
                    continue;
                }

                final String[] addrAndPort = params[1].split( ":" );
                if ( null == addrAndPort )
                {
                    continue;
                }
                if ( addrAndPort.length < 1 )
                {
                    continue;
                }
                if ( ! "00000000".equals( addrAndPort[0] ) )
                {
                    continue;
                }
                {
                    int port = 0;
                    {
                        try
                        {
                            port = Integer.parseInt(addrAndPort[1], 16);
                        }
                        catch (NumberFormatException e)
                        {
                            DebugLog.d(TAG, "", e);
                        }
                    }
                    if ( 56 != (port/1000) )
                    {
                        continue;
                    }

                    if ( portDebug == port )
                    {
                        isRunningMonoDebug = true;
                        break;
                    }
                }
            }
        }
        catch ( FileNotFoundException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        finally
        {
            if ( null != br )
            {
                try { br.close(); } catch ( Exception eClose ) { }
            }
        }

        return isRunningMonoDebug;
    }

}
