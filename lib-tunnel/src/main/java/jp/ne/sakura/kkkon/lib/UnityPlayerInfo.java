package jp.ne.sakura.kkkon.lib;

/**
 * Created by kkkon on 2017/11/11.
 */

public class UnityPlayerInfo
{
    public String mIP;
    public int mPort;
    public long mFlags;
    public long mGuid;
    public long mEditorId;
    public int mVersion;
    public String mId;
    public int mDebugPort;
    public boolean mAllowDebug;

    @Override
    public String toString()
    {
        return "Info{" +
                "mIP='" + mIP + '\'' +
                ", mPort=" + mPort +
                ", mFlags=" + mFlags +
                ", mGuid=" + mGuid +
                ", mEditorId=" + mEditorId +
                ", mVersion=" + mVersion +
                ", mId='" + mId + '\'' +
                ", mDebugPort=" + mDebugPort +
                ", mAllowDebug=" + mAllowDebug +
                '}';
    }

    public void setIP( final String ip )
    {
        mIP = ip;
    }

    public String generateMessage()
    {
        String message = String.format( "[IP] %s [Port] %d [Flags] %d [Guid] %d [EditorId] %d [Version] %d [Id] %s [Debug] %d"
                , mIP, mPort, mFlags, mGuid, mEditorId, mVersion, mId, ((mAllowDebug)?(1):(0))
        );

        return message;
    }

    public int getDebugPort()
    {
        if ( 0 != mDebugPort )
        {
            return mDebugPort;
        }

        final int value = 56000 + (int)(mGuid % 1000);
        return value;
    }

    public static UnityPlayerInfo parse( final String line )
    {
        if ( null == line )
        {
            return null;
        }

        final String[] split = line.split(" ");
        if ( null == split )
        {
            return null;
        }

        UnityPlayerInfo info = new UnityPlayerInfo();

        final int count = split.length;
        for ( int indexSplit = 0; indexSplit < count; ++indexSplit )
        {
            final String param = split[indexSplit+0];
            //System.out.println( param );
            if ( 0 == "[IP]".compareTo(param) )
            {
                info.mIP = split[indexSplit+1];
            }
            else
            if ( 0 == "[Port]".compareTo(param) )
            {
                try
                {
                    info.mPort = Integer.parseInt(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
            }
            else
            if ( 0 == "[Flags]".compareTo(param) )
            {
                try
                {
                    info.mFlags = Long.parseLong(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
            }
            else
            if ( 0 == "[Guid]".compareTo(param) )
            {
                try
                {
                    info.mGuid = Long.parseLong(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
            }
            else
            if ( 0 == "[EditorId]".compareTo(param) )
            {
                try
                {
                    info.mEditorId = Long.parseLong(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
            }
            else
            if ( 0 == "[Version]".compareTo(param) )
            {
                try
                {
                    info.mVersion = Integer.parseInt(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
            }
            else
            if ( 0 == "[Id]".compareTo(param) )
            {
                //info.mId = split[indexSplit+1];
                final String s = split[indexSplit+1];
                final int index = s.indexOf( ':' );
                if ( 0 < index )
                {
                    final String port = s.substring(index+1);
                    try
                    {
                        info.mDebugPort = Integer.parseInt(port);
                    }
                    catch ( NumberFormatException e )
                    {

                    }
                    info.mId = s.substring( 0, index );
                }
                else
                {
                    info.mId = s;
                }
            }
            else
            if ( 0 == "[Debug]".compareTo(param) )
            {
                int value = 0;
                try
                {
                    value = Integer.parseInt(split[indexSplit + 1]);
                }
                catch ( NumberFormatException e )
                {

                }
                if ( 0 != value )
                {
                    info.mAllowDebug = true;
                }
            }
        }

        //System.out.println( info );
        return info;
    }

}
