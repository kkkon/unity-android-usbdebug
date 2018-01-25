using UnityEngine;
using System.Collections;


#if UNITY_EDITOR

using UnityEditor;

namespace UniAndUSBDebug
{

public class UnityAndroidUSBDebug
{
    private static string KEY_JDK = "JdkPath";
    private static string KEY_ANDROID_SDK = "AndroidSdkRoot";
    private static string KEY_ANDROID_NDK = "AndroidNdkRoot";

    static void invoke( string name, string[] args, bool waitFinish = true )
    {
        var info = new System.Diagnostics.ProcessStartInfo();

        if ( EditorPrefs.HasKey(KEY_ANDROID_SDK) )
        {
            info.EnvironmentVariables["ANDROID_SDK_ROOT"] = EditorPrefs.GetString(KEY_ANDROID_SDK);
        }
        //info.EnvironmentVariables["ANDROID_SERIAL"] = "xxxx"; // for adb
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

        info.RedirectStandardOutput = true;
        info.RedirectStandardError = true;

        var process = new System.Diagnostics.Process();
        process.StartInfo = info;

        var sbOut = new System.Text.StringBuilder();
        var sbErr = new System.Text.StringBuilder();

        process.OutputDataReceived += (sender, eventArgs) => sbOut.AppendLine(eventArgs.Data);
        process.ErrorDataReceived += (sender, eventArgs) => sbErr.AppendLine(eventArgs.Data);

        try
        {
            //Debug.Log( name + " " + info.Arguments );
            process.Start();

            process.BeginOutputReadLine();
            process.BeginErrorReadLine();

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

}

} // namespace UniAndUSBDebug

#endif // UNITY_EDITOR
