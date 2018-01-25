# unity-android-usbdebug

Unity application enabled 'Script Debug' multicast packet.
But it only network not over usb.

These applications that multicast packet relay over usb.

## how to use

- goto releases(https://github.com/kkkon/unity-android-usbdebug/releases)
- download and extract UnityAndroidUSBDebug_vX.X.zip to your unity project or empty unity project
- select Unity menu 'UniAndUSBDebug > Install APK'
- launch application, named UnityAndroidUSBDebug
  - click 'listen start' button
- launch your application
- select Unity menu 'UniAndUSBDebug > Start'
- MonoDevelop will find out 'AndroidPlayer @127.0.0.1'

## internals

Android application(app-target) and Java application(app-host)
relay multicast packet over usb.

Android application relay modified multicast packet over usb.

Java application multicast to host machine come from usb.

Java application append 'adb forward tcp:56XXX tcp:56XXX',
which port used by mono debugger.


