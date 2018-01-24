package jp.ne.sakura.kkkon.lib;

import android.util.Log;

/**
 * Created by kkkon on 2017/12/06.
 */

public class DebugLog
{
    public static void v( final String tag, final String message )
    {
        if ( BuildConfig.DEBUG )
        {
            Log.v( tag,message );
        }
    }
    public static void v( final String tag, final String message, final Throwable t )
    {
        if ( BuildConfig.DEBUG )
        {
            Log.v( tag,message, t );
        }
    }

    public static void d( final String tag, final String message )
    {
        if ( BuildConfig.DEBUG )
        {
            Log.d( tag,message );
        }
    }
    public static void d( final String tag, final String message, final Throwable t )
    {
        if ( BuildConfig.DEBUG )
        {
            Log.d( tag,message, t );
        }
    }

    public static void e( final String tag, final String message )
    {
        Log.e( tag,message );
    }
    public static void e( final String tag, final String message, final Throwable t )
    {
        Log.e( tag,message, t );
    }

}
