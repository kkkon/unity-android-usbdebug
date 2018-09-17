using UnityEngine;
using System.Collections;
using System.Collections.Generic;


#if UNITY_EDITOR

using UnityEditor;

namespace UniAndUSBDebug
{

public class UnityAndroidUSBDebug
{
    private static string KEY_JDK = "JdkPath";
    private static string KEY_ANDROID_SDK = "AndroidSdkRoot";
    private static string KEY_ANDROID_NDK = "AndroidNdkRoot";

    private static string mDeviceSerial = string.Empty;

    static string invoke( string name, string[] args, bool waitFinish = true, bool redirectStrandard = true )
    {
        var info = new System.Diagnostics.ProcessStartInfo();

        if ( EditorPrefs.HasKey(KEY_ANDROID_SDK) )
        {
            info.EnvironmentVariables["ANDROID_SDK_ROOT"] = EditorPrefs.GetString(KEY_ANDROID_SDK);
        }
        if ( 0 < mDeviceSerial.Length )
        {
            //Debug.Log( "mDeviceSerial.Length=" + mDeviceSerial.Length );
            info.EnvironmentVariables["ANDROID_SERIAL"] = mDeviceSerial; // for adb
        }
        info.UseShellExecute = false;

        info.FileName = name;
        if ( null != args )
        {
            string cmdArgs = "";
            bool needSpace = false;
            foreach ( string arg in args )
            {
                if ( needSpace )
                {
                    cmdArgs += " ";
                }
                else
                {
                    needSpace = true;
                }
                cmdArgs += arg;
            }
            info.Arguments = cmdArgs;
        }

        if ( redirectStrandard )
        {
            info.RedirectStandardOutput = true;
            info.RedirectStandardError = true;
        }

        var process = new System.Diagnostics.Process();
        process.StartInfo = info;

        var sbOut = new System.Text.StringBuilder();
        var sbErr = new System.Text.StringBuilder();

        if ( redirectStrandard )
        {
            process.OutputDataReceived += (sender, eventArgs) => sbOut.AppendLine(eventArgs.Data);
            process.ErrorDataReceived += (sender, eventArgs) => sbErr.AppendLine(eventArgs.Data);
        }

        try
        {
            //Debug.Log( name + " " + info.Arguments );
            process.Start();

            if ( redirectStrandard )
            {
                process.BeginOutputReadLine();
                process.BeginErrorReadLine();
            }

            if ( waitFinish )
            {
                for ( int retry = 0; retry < 5; ++retry )
                {
                    var isExited = process.WaitForExit( 60*1000 );
                    if ( isExited )
                    {
                        break;
                    }
                }

                //Debug.Log( "STDOUT: " + sbOut.ToString() );
                //Debug.Log( "STDERR: " + sbErr.ToString() );
                if ( 0 != process.ExitCode )
                {
                    throw new System.Exception("ExitCode=" + process.ExitCode + System.Environment.NewLine
                        + "STDOUT: " + sbOut.ToString() + System.Environment.NewLine
                        + "STDERR: " + sbErr.ToString() + System.Environment.NewLine
                    );
                }
            }
        }
        catch ( System.Exception e )
        {
            Debug.LogError( e );
        }
        finally
        {
            try { process.Close(); } catch ( System.Exception ) { }
            try { process.Dispose(); } catch ( System.Exception ) { }
        }

        string output = null;
        output = sbOut.ToString();
        return output;
    }

    //[MenuItem("UniAndUSBDebug/Test")]
    static void Test()
    {
        if ( EditorPrefs.HasKey(KEY_JDK) )
        {
            Debug.Log( "Jdk:" + EditorPrefs.GetString(KEY_JDK) );
        }
        if ( EditorPrefs.HasKey(KEY_ANDROID_SDK) )
        {
            Debug.Log( "Sdk:" + EditorPrefs.GetString(KEY_ANDROID_SDK) );
        }
        if ( EditorPrefs.HasKey(KEY_ANDROID_NDK) )
        {
            Debug.Log( "Ndk:" + EditorPrefs.GetString(KEY_ANDROID_NDK) );
        }
    }

    [MenuItem("UniAndUSBDebug/Start")]
    static void Start()
    {
        invoke( EditorPrefs.GetString(KEY_JDK) + "/bin/java", new string[] {
            "-jar"
            , "Assets/UnityAndroidUSBDebug/Editor/app-host-shaded.jar"
            }
            , false
            , false // redirectStandard
        );
    }

    [MenuItem("UniAndUSBDebug/Stop")]
    static void Stop()
    {
        invoke( EditorPrefs.GetString(KEY_JDK) + "/bin/java", new string[] {
            "-jar"
            , "Assets/UnityAndroidUSBDebug/Editor/app-host-shaded.jar"
            , "--stop"
        });

        invoke( EditorPrefs.GetString(KEY_ANDROID_SDK) + "/platform-tools/adb", new string[] {
            "forward"
            , "--remove-all"
        });
    }

    [MenuItem("UniAndUSBDebug/Install APK")]
    static void InstallApk()
    {
        invoke( EditorPrefs.GetString(KEY_ANDROID_SDK) + "/platform-tools/adb", new string[] {
            "install"
            , "-r"
            , "Assets/UnityAndroidUSBDebug/Editor/app-target-debug.apk"
        });
    }




    public class DeviceInfo
    {
        public string mSerial;
        public string mDesc;
    }

    public static List<DeviceInfo> getDevices()
    {
        string output = null;

        output = invoke( EditorPrefs.GetString(KEY_ANDROID_SDK) + "/platform-tools/adb", new string[] {
            "devices"
            , "-l"
        });

        if ( null == output )
        {
            return null;
        }

        //Debug.Log( output );
        string[] lines = output.Split( '\n' );
        if ( null == lines )
        {
            return null;
        }

        List<DeviceInfo> listDevInfo = new List<DeviceInfo>();
        {
            var devInfo = new DeviceInfo();
            devInfo.mSerial = "     "; //string.Empty;
            devInfo.mDesc = "(no select)";
            listDevInfo.Add( devInfo );
        }
        foreach ( var line in lines )
        {
            if ( line.StartsWith( "List" ) )
            {
                continue;
            }
            if ( line.StartsWith( "adb server" ) )
            {
                continue;
            }
            if ( line.StartsWith( "* daemon" ) )
            {
                continue;
            }

            string[] tokens = line.Split( new char[] { ' ', '\t' }, 2 );
            if ( null == tokens )
            {
                continue;
            }
            if ( tokens.Length < 2 )
            {
                continue;
            }
            //Debug.Log( tokens[0] + "," + tokens[1] );
            tokens[0] = tokens[0].Trim();
            tokens[1] = tokens[1].Trim();

            var devInfo = new DeviceInfo();
            devInfo.mSerial = tokens[0];
            devInfo.mDesc = tokens[1];
            listDevInfo.Add( devInfo );
        }

        return listDevInfo;
    }

    public static string getDeviceSerial()
    {
        return mDeviceSerial;
    }

    public static void setDeviceSerial( string serial )
    {
        if ( null == serial )
        {
            serial = string.Empty;
        }
        mDeviceSerial = serial.Trim();
    }

    [MenuItem("UniAndUSBDebug/SelectDevice", false, 1000)]
    static void SelectDevice()
    {
        UniAndDeviceSelect.Init();
    }
}

public class UniAndDeviceSelect: EditorWindow
{
    private static List<UnityAndroidUSBDebug.DeviceInfo> mDeviceInfo;
    private static int mIndex = 0;
    private static GUIContent[] mContent;

    public static void selectSerial( string serial )
    {
        var count = mDeviceInfo.Count;
        mIndex = 0;
        for ( int index = 0; index < count; ++index )
        {
            if ( serial.Equals( mContent[index].text ) )
            {
                mIndex = index;
            }
        }
    }

    public static void Init()
    {
        var window = GetWindow( typeof(UniAndDeviceSelect) );
        ListRefresh();
        window.Show();
    }

    static void ListRefresh()
    {
        mDeviceInfo = UnityAndroidUSBDebug.getDevices();

        var serial = string.Empty;
        if ( null == mContent )
        {
            serial = UnityAndroidUSBDebug.getDeviceSerial();
        }
        else
        {
            serial = mContent[mIndex].text;
        }

        {
            var count = mDeviceInfo.Count;
            mContent = new GUIContent[count];
            for ( int index = 0; index < count; ++index )
            {
                var content = new GUIContent();
                content.text    = mDeviceInfo[index].mSerial;
                content.tooltip = mDeviceInfo[index].mDesc;
                mContent[index] = content;
            }
        }

        selectSerial( serial );
    }

    void OnGUI()
    {
        if ( GUILayout.Button("Refresh") )
        {
            ListRefresh();
        }

        var index = EditorGUILayout.Popup( mIndex, mContent );
        if ( index != mIndex )
        {
            UnityAndroidUSBDebug.setDeviceSerial( mContent[index].text );
        }
        mIndex = index;
    }
}


} // namespace UniAndUSBDebug

#endif // UNITY_EDITOR
