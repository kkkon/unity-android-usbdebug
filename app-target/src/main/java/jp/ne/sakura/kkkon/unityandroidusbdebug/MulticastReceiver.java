package jp.ne.sakura.kkkon.unityandroidusbdebug;

import jp.ne.sakura.kkkon.lib.DebugLog;
import jp.ne.sakura.kkkon.lib.UnityMulticastTunnel;
import jp.ne.sakura.kkkon.lib.UnityPlayerInfo;

/**
 * Created by kkkon on 2017/11/11.
 */

public class MulticastReceiver
{
    private static Object mLock = new Object();
    private static Thread mThread = null;

    public static void threadStart()
    {
        synchronized (mLock)
        {
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
                        continue;
                    }

                    DebugLog.d( "", ni.getDisplayName() );
                    DebugLog.d( "", "  " );
                    DebugLog.d( "", ni.getInterfaceAddresses().toString() );
                }
            }
            catch ( java.net.SocketException e )
            {

            }
            if ( null == mThread )
            {
                mThread = new ThreadRecv();
                mThread.start();
            }
        }
    }

    public static void threadStop()
    {
        synchronized (mLock)
        {
            if ( null != mThread )
            {
                mThread.interrupt();
                try
                {
                    mThread.join();
                }
                catch ( InterruptedException e )
                {
                    DebugLog.d( "", "exception", e );
                }
            }
        }
    }

    private static class ThreadRecv extends Thread
    {
        @Override
        public void run()
        {
            for ( ; ; )
            {
                try
                {
                    UnityPlayerInfo info = UnityMulticastTunnel.poll();
                    if ( null != info )
                    {
                        MulticastTunnel.pushPlayerInfo( info );
                    }

                    Thread.sleep(30 * 1000);
                }
                catch ( InterruptedException e )
                {
                    break;
                }
            }
        }
    }
}
