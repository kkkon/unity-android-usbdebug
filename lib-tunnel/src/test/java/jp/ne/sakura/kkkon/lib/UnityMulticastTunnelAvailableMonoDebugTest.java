package jp.ne.sakura.kkkon.lib;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kkkon on 2018/01/22.
 */
public class UnityMulticastTunnelAvailableMonoDebugTest {
    private static final String TAG = "UnityMulticastTunnelAvailableMonoDebugTest";

    @Test
    public void test() throws Exception
    {
        final String[] lines = new String[] {
              "   sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode"
            , "    0: 00000000:DCEA 00000000:0000 0A 00000000:00000000 00:00000000 00000000 10056        0 27741 1 00000000 100 0 0 10 0"
            , "    1: 00000000:D82D 00000000:0000 0A 00000000:00000000 00:00000000 00000000 10056        0 27567 1 00000000 100 0 0 10 0"
            , "    2: 0100007F:13AD 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0        0 455 1 00000000 100 0 0 10 0"
            , "    3: 0100007F:DCEA 0100007F:C23A 08 00000000:0000001F 00:00000000 00000000 10056        0 49387 1 00000000 25 4 24 10 -1"
            , "    4: 0100007F:DCEA 0100007F:C23D 08 00000000:00000010 00:00000000 00000000 10056        0 50942 1 00000000 25 4 26 10 -1"

            , null
            , ""
        };

        for ( final String line : lines )
        {
            if ( null == line )
            {
                continue;
            }

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
            DebugLog.d( TAG, params[1] + " " + params[2] );

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
            }

        } // for
    }


    @Test
    public void availableMonoDebug() throws Exception
    {
    }

}