package jp.ne.sakura.kkkon.lib;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by kkkon on 2017/11/11.
 */
public class UnityPlayerInfoParseTest
{
    @Test
    public void parse() throws Exception
    {
        {
            final String line = "[IP] 192.168.110.128 [Port] 55194 [Flags] 3 [Guid] 2477256561 [EditorId] 1495077346 [Version] 1048832 [Id] WindowsPlayer(kkkon) [Debug] 1";
            UnityPlayerInfo info = null;
            info = UnityPlayerInfo.parse(line);
            System.out.println(info);
        }
        {
            final String line = "[IP] 10.0.2.15 [Port] 55290 [Flags] 3 [Guid] 3311816623 [EditorId] 1021448195 [Version] 1048832 [Id] AndroidPlayer(unknown_sdk_phone_armv7@10.0.2.15) [Debug] 1";
            UnityPlayerInfo info = null;
            info = UnityPlayerInfo.parse(line);
            System.out.println(info);
        }
    }

    @Test
    public void test1() throws Exception
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

            System.out.println( ni.getDisplayName() );
            System.out.print( "  " );
            System.out.println( ni.getInterfaceAddresses() );
        }
    }

}