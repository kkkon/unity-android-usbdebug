package jp.ne.sakura.kkkon.lib;

/**
 * Created by kkkon on 2017/12/06.
 */

public class DebugLog
{
    public static void v( final String tag, final String message )
    {
        System.out.println( message );
    }
    public static void v( final String tag, final String message, final Throwable t )
    {
        System.out.println( message );
        t.printStackTrace( System.out );
    }

    public static void d( final String tag, final String message )
    {
        System.out.println( message );
    }
    public static void d( final String tag, final String message, final Throwable t )
    {
        System.out.println( message );
        t.printStackTrace( System.out );
    }

    public static void e( final String tag, final String message )
    {
        System.err.println( message );
    }
    public static void e( final String tag, final String message, final Throwable t )
    {
        System.err.println( message );
        t.printStackTrace( System.err );
    }

}
