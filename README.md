
Cross platform libGDX based demo project showing concept of the Verlet integration.


How run (Windows):

- install JRE and JDK 8 (skip, if already installed)

- install Android SDK (skip, if already installed)

- in command shell (like cmd.exe) type commands:

  set JAVA_HOME=full_path_to_jre
  set ANDROID_HOME=full_path_to_android_sdk

- for Desktop module type command in project folder:

  gradlew.bat :desktop:run

- for building Android apk-file type command in project folder:
  (debug version recommended for use on real devices)

  gradlew.bat :android:build
