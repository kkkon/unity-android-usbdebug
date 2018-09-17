# unity-android-usbdebug

Unity application enabled 'Script Debug' multicast packet.
But it only network, not over usb.

These applications relay multicast packet over usb, from Android target to Desktop host.  
And these application relay only 'Unity's Debug Multicast', which find target on C# debugger.

## how to use

- goto releases(https://github.com/kkkon/unity-android-usbdebug/releases)
- download and extract UnityAndroidUSBDebug_vX.X.zip to your unity project or empty unity project
- select Unity menu 'UniAndUSBDebug > Install APK'
- enable WiFi. (Unity does not use 'lo' interface.)
- launch application, named UnityAndroidUSBDebug
  - click 'listen start' button
- launch your application
- select Unity menu 'UniAndUSBDebug > Start'
- wait a few minutes.
- MonoDevelop will find out 'AndroidPlayer @127.0.0.1'

## please check, when not work

- Development Build
- Script Debugging
- Scripting Backend: Mono

### if not work break point

- rebuild and re-install apk and run correct apk

## internals

Android application(app-target) and Java application(app-host)
relay multicast packet over usb.

Android application relay modified multicast packet over usb.

Java application multicast to host machine come from usb.

Java application append 'adb forward tcp:56XXX tcp:56XXX',
which port used by mono debugger.


## tested

#### Moto G4 Plus(Android 6.0.1 MPJ24.139-48)
- Win7, Unity 5.4.5f1 MonoDeveloper
- Win7, Unity 5.6.6f2 MonoDeveloper
- Win7, Unity 2017.4.8f1 MonoDeveloper

#### Moto G6 Plus(Android 8.0.0 OPW27.113-45)
- Win7, Unity 5.4.5f1 MonoDeveloper 
- Win7, Unity 5.6.6f2 MonoDeveloper
- Win7, Unity 2017.4.8f1 MonoDeveloper

