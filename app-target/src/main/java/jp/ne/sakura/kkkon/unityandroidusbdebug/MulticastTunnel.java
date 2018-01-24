package jp.ne.sakura.kkkon.unityandroidusbdebug;

import jp.ne.sakura.kkkon.lib.DebugLog;
import jp.ne.sakura.kkkon.lib.UnityMulticastTunnel;
import jp.ne.sakura.kkkon.lib.UnityMulticastTunnelTarget;
import jp.ne.sakura.kkkon.lib.UnityPlayerInfo;

/**
 * Created by kkkon on 2017/11/11.
 */

public class MulticastTunnel
{
    private static Object mLock = new Object();
    private static Thread mThread = null;

    private static UnityPlayerInfo mInfo = null;

    public static void pushPlayerInfo( UnityPlayerInfo info )
    {
        synchronized (mLock)
        {
            mInfo = info;
        }
    }

    public static void threadStart()
    {
        synchronized (mLock)
        {
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
            UnityMulticastTunnelTarget.init();

            for ( ; ; )
            {
                try
                {
                    if ( UnityMulticastTunnelTarget.accept() )
                    {
                        while ( true )
                        {
                            UnityPlayerInfo info = null;
                            synchronized (mLock)
                            {
                                info = mInfo;
                            }
                            if ( null != info )
                            {
                                final boolean sended = UnityMulticastTunnelTarget.relay(info);
                                if ( !sended )
                                {
                                    break;
                                }
                                Thread.sleep(10 * 1000);
                            }
                        } // while
                    }
                    Thread.sleep(30 * 1000);
                }
                catch ( InterruptedException e )
                {
                    break;
                }
            }

            UnityMulticastTunnelTarget.term();
        }
    }
}
