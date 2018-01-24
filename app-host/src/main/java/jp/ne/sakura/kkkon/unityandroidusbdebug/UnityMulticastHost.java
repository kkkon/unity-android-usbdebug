package jp.ne.sakura.kkkon.unityandroidusbdebug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import jp.ne.sakura.kkkon.lib.DebugLog;
import jp.ne.sakura.kkkon.lib.UnityMulticastTunnelHost;
import jp.ne.sakura.kkkon.lib.UnityPlayerConst;
import jp.ne.sakura.kkkon.lib.UnityPlayerInfo;

/**
 * Created by kkkon on 2018/01/22.
 */

public class UnityMulticastHost
{
    private static final String TAG = "UnityMulticastHost";

    private static final int MY_APP_PORT = UnityPlayerConst.PLAYER_MULTICAST_PORT - 10000;

    private static ServerSocket mSocketServer = null;
    private static boolean mNeedStop = false;

    public static String findAndroidSDK_PlatformTools()
    {
        {
            final String sdkRoot = System.getenv("ANDROID_SDK_ROOT");
            File dir = new File( sdkRoot, "platform-tools");
            if ( dir.exists() )
            {
                return dir.getAbsolutePath();
            }
        }

        {
            final String sdkRootLegacy = System.getenv("ANDROID_HOME");
            File dir = new File(sdkRootLegacy, "platform-tools");
            if (dir.exists()) {
                return dir.getAbsolutePath();
            }
        }

        return null;
    }

    public static void executeCmd( String[] args )
    {
        assert null != args;
        ProcessBuilder pb = new ProcessBuilder( args );
        Process process = null;
        try
        {
            process = pb.start();

            int ret = process.waitFor();
            DebugLog.d( TAG, pb.command().toString() );
            DebugLog.d( TAG, " ret=" + ret );
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        catch ( InterruptedException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        finally
        {
            if ( null != process )
            {
                try { process.destroy(); } catch ( Exception eClose ) { }
            }
        }
    }

    public static boolean start()
    {
        ServerSocket socketServer = null;
        SocketAddress addr = null;
        try
        {
            addr = new InetSocketAddress("127.0.0.1", MY_APP_PORT );

            socketServer = new ServerSocket();
            socketServer.setReuseAddress( false );
            socketServer.setSoTimeout( 3*1000 );

            socketServer.bind( addr );

            mSocketServer = socketServer;
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
            if ( null != socketServer )
            {
                try { socketServer.close(); } catch ( Exception eClose ) {}
                socketServer = null;
            }
            return false;
        }

        return true;
    }

    public static void stop()
    {
        Socket socket = null;
        SocketAddress addr = null;
        OutputStream out = null;
        try
        {
            addr = new InetSocketAddress("127.0.0.1", MY_APP_PORT );

            socket = new Socket();
            socket.setSoTimeout( 3*1000 );

            socket.connect( addr );

            out = socket.getOutputStream();
            final byte[] message = "stop\r\n".getBytes();
            out.write( message );
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        finally
        {
            if ( null != out )
            {
                try { out.close(); } catch ( Exception eClose ) {}
                out = null;
            }
            if ( null != socket )
            {
                try { socket.close(); } catch ( Exception eClose ) {}
                socket = null;
            }
        }

    }

    public static void term()
    {
        if ( null != mSocketServer )
        {
            try { mSocketServer.close(); } catch ( Exception eClose ) {}
            mSocketServer = null;
        }
    }

    public static boolean poll()
    {
        boolean result = false;

        Socket socket = null;
        InputStream in = null;
        try
        {
            socket = mSocketServer.accept();
            in = socket.getInputStream();
            byte[] buff = new byte[1024];

            // TODO split line
            final int readed = in.read( buff );
            if ( 0 < readed )
            {
                final String line = new String(buff, 0, readed);
                if ( "stop\r\n".equals(line) )
                {
                    mNeedStop = true;
                    result = true;
                }
            }
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        finally
        {
            if ( null != in )
            {
                try { in.close(); } catch ( Exception eClose ) {}
                in = null;
            }
            if ( null != socket )
            {
                try { socket.close(); } catch ( Exception eClose ) {}
                socket = null;
            }
        }

        return result;
    }



    public static void main( String[] args )
    {
        final String androidPlatformTools = findAndroidSDK_PlatformTools();
        if ( null == androidPlatformTools )
        {
            DebugLog.e( TAG, "please set environment 'ANDROID_SDK_ROOT' or 'ANDROID_HOME'" );
            return;
        }

        boolean optStop = false;
        if ( null != args )
        {
            for ( final String arg : args )
            {
                if ( "--stop".equals(arg) )
                {
                    optStop = true;
                }
            }
        }

        if ( optStop )
        {
            stop();

            return;
        }

        if ( !start() )
        {
            return;
        }

        // "adb forward --remove-all
        executeCmd( new String[] {
                androidPlatformTools + File.separator + "adb"
                , "forward"
                , "--remove-all"
        });

        // "adb forward tcp:54997 tcp:54997"
        executeCmd( new String[] {
                androidPlatformTools + File.separator + "adb"
                , "forward"
                , "tcp:" + UnityPlayerConst.PLAYER_MULTICAST_PORT
                , "tcp:" + UnityPlayerConst.PLAYER_MULTICAST_PORT
        });

        while ( true )
        {
            if ( poll() )
            {
                if ( mNeedStop )
                {
                    break;
                }
            }

            UnityPlayerInfo info = UnityMulticastTunnelHost.poll();
            if ( null != info )
            {
                final int portDebug = info.getDebugPort();
                // "adb forward tcp:portDebug tcp:portDebug"
                executeCmd( new String[] {
                        androidPlatformTools + File.separator + "adb"
                        , "forward"
                        , "tcp:" + portDebug
                        , "tcp:" + portDebug
                });

                for ( int count = 0; count < 10; count++ )
                {
                    UnityMulticastTunnelHost.relay(info);
                    try
                    {
                        Thread.sleep(3 * 1000);
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }

            try
            {
                Thread.sleep(10 * 1000);
            }
            catch ( InterruptedException e )
            {
                DebugLog.d( "UnityMulticastHost", "exception", e );
                break;
            }
        } // while

        term();

        return;
    }



}
