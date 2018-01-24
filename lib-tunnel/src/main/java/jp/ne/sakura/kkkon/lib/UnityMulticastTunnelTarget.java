package jp.ne.sakura.kkkon.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by kkkon on 2017/11/11.
 */

public class UnityMulticastTunnelTarget
{
    private static final String TAG = "UnityMulticastTunnelTarget";

    protected static ServerSocket mSocketServer = null;
    protected static Socket mSocketClient = null;

    public static void init()
    {
        ServerSocket socketServer = null;
        SocketAddress addr = null;

        assert null == mSocketServer;
        try
        {
            addr = new InetSocketAddress("127.0.0.1", UnityPlayerConst.PLAYER_MULTICAST_PORT );
            socketServer = new ServerSocket();
            socketServer.setReuseAddress( true );
            socketServer.setSoTimeout( 3*1000 );

            socketServer.bind( addr );

            mSocketServer = socketServer;
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
        if ( null != mSocketClient )
        {
            try { mSocketClient.close(); } catch ( Exception e ) {}
            mSocketClient = null;
        }
        if ( null != mSocketServer )
        {
            try { mSocketServer.close(); } catch ( Exception e ) {}
            mSocketServer = null;
        }
    }

    public static boolean accept()
    {
        assert null != mSocketServer;
        Socket socket = null;
        try
        {
            socket = mSocketServer.accept();
            if ( null != mSocketClient )
            {
                try { mSocketClient.close(); } catch ( Exception e ) {}
                mSocketClient = null;
            }
            mSocketClient = socket;
        }
        catch ( SocketTimeoutException e )
        {

        }
        catch ( SocketException e )
        {
            term();
        }
        catch ( IOException e )
        {
            term();
        }

        if ( null == socket )
        {
            return false;
        }

        return true;
    }

    public static boolean relay( final UnityPlayerInfo info )
    {
        assert null != mSocketClient;

        OutputStream out = null;
        try
        {
            mSocketClient.setSoTimeout( 10 * 1000 );

            {
                out = mSocketClient.getOutputStream();
                final String ipOrig = info.mIP;
                final String idOrig = info.mId;
                info.setIP("127.0.0.1");
                info.mId = idOrig.replace( "@" + ipOrig, "@127.0.0.1" );
                final String line = info.generateMessage();
                byte[] buff = line.getBytes();
                out.write( buff );
            }

        }
        catch ( SocketException e )
        {
            DebugLog.e( TAG, "", e );
            if ( null != mSocketClient )
            {
                try { mSocketClient.close(); } catch ( Exception eClose ) {}
                mSocketClient = null;
            }
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "", e );
            if ( null != mSocketClient )
            {
                try { mSocketClient.close(); } catch ( Exception eClose ) {}
                mSocketClient = null;
            }
        }

        if ( null == mSocketClient )
        {
            return false;
        }
        return true;
    }
}
