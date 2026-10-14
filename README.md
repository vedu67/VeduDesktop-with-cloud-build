Vedu Desktop (Android)
A starter Android app that creates a desktop-like workspace inside the app. It includes a desktop wallpaper, shortcuts, a taskbar, a start menu, quick panel, and movable/minimizable/maximizable windows for Notes, Calculator, Files demo, and About.
Build with Android Studio
Install the current stable Android Studio.
Open Android Studio and choose Open.
Select the VeduDesktop folder (the folder containing settings.gradle.kts).
Let Gradle sync. If prompted, install Android SDK Platform 35.
Connect your Android phone using USB and enable Developer options > USB debugging, or create an emulator.
Press Run ▶ and select your device.
Build an APK
In Android Studio, choose Build > Build Bundle(s) / APK(s) > Build APK(s). The APK is usually created at:
app/build/outputs/apk/debug/app-debug.apk
Transfer that APK to your phone and open it to install. Android may ask you to allow installation from that source. Only install APKs you built or trust.
Requirements
Android Studio with JDK 17
Android SDK Platform 35
Android 8.0 (API 26) or later
Current limitations
This is a desktop-style interface contained inside one Android app, not a replacement for Android's system UI.
The Files app is currently a demo list; it does not yet browse real storage. A real file picker can be added.
Notes are held in memory and reset when the app process is closed.
External monitor support depends on phone hardware and Android support; this starter app does not create a separate external-display session.
App windows are UI panels inside the app, not separate Android app windows.