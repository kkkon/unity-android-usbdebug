package jp.ne.sakura.kkkon.unityandroidusbdebug;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

import jp.ne.sakura.kkkon.lib.DebugLog;
import jp.ne.sakura.kkkon.lib.UnityPlayerConst;

import static org.junit.Assert.*;

/**
 * Created by kkkon on 2018/01/23.
 */
public class UnityMulticastHostTest
{
    private static final String TAG = "UnityMulticastHostTest";

    private static final int MY_APP_PORT = UnityPlayerConst.PLAYER_MULTICAST_PORT - 10000;

    @Test
    public void test1() throws Exception
    {
        ServerSocket socketServer1 = null;
        ServerSocket socketServer2 = null;
        SocketAddress addr = null;
        try
        {
            addr = new InetSocketAddress("127.0.0.1", MY_APP_PORT );

            {
                socketServer1 = new ServerSocket();
                socketServer1.setReuseAddress(false);
                socketServer1.setSoTimeout(3 * 1000);

                socketServer1.bind(addr);
            }

            {
                socketServer2 = new ServerSocket();
                socketServer2.setReuseAddress(false);
                socketServer2.setSoTimeout(3 * 1000);

                socketServer2.bind(addr);
            }
        }
        catch ( IOException e )
        {
            DebugLog.e( TAG, "exception", e );
        }
        finally
        {
            if ( null != socketServer1 )
            {
                try { socketServer1.close(); } catch ( Exception eClose ) {}
                socketServer1 = null;
            }
            if ( null != socketServer2 )
            {
                try { socketServer2.close(); } catch ( Exception eClose ) {}
                socketServer2 = null;
            }
        }

    }

    @Test
    public void start() throws Exception
    {
    }

    @Test
    public void stop() throws Exception
    {
    }

}