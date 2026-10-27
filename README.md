# Clean Launcher for Fire TV

A quiet home screen for the Fire TV, modelled on
[CleanFrame](https://github.com/nthnclrk/CleanFrame) (Samsung The Frame / Tizen)
and rebuilt as a native Android app. This is John Nutter's fork of
[Hector's AmazonFire build](https://github.com/Haguilar91/CleanFrameAmazonFire).

A photograph, the time, and a frosted-glass dock of your apps.

## Changes in this fork

- The cover over Amazon's home now stays up until Fire OS has finished fading
  Clean Launcher in. Previously Amazon's home flickered through about 5 s
  after pressing Home.
- A pick made on the cover just as Clean Launcher took over no longer leaves a
  timer behind that could pull down the next cover.
- The dock is only rebuilt when it has changed, so the cover appears sooner
  after Home (most noticeable when leaving Netflix).

## Install

    ./install.sh                 # defaults to 192.168.50.231
    ./install.sh 192.168.x.x     # another TV

`CleanLauncher.apk` is prebuilt. The script installs it and enables the
accessibility service that sends the Home button here (Fire OS can't change
the default launcher).

### Manual install

Use this to install your own build, or to keep any accessibility services you
already have enabled. Turn on ADB debugging in the TV's Developer Options first.

    adb connect <tv-ip>:5555
    # Note this value; it's what you restore when uninstalling.
    adb shell settings get secure enabled_accessibility_services
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    # If the value above was null, use just the service; otherwise append
    # ":com.hector.cleanlauncher/.HomeWatcherService" to it.
    adb shell settings put secure enabled_accessibility_services com.hector.cleanlauncher/.HomeWatcherService
    adb shell settings put secure accessibility_enabled 1

### Fast Home detection (optional)

Without it, Clean Launcher only notices Home once Amazon's home appears, so
you see Amazon's home briefly, presses made while Amazon's home is showing are
missed, and "Press twice for Amazon" doesn't work. Granting log access lets it
see Home presses as Fire OS logs them:

    adb shell pm grant com.hector.cleanlauncher android.permission.READ_LOGS
    adb shell pm revoke com.hector.cleanlauncher android.permission.READ_LOGS   # undo

READ_LOGS can read every app's log output. Clean Launcher filters to
ActivityManager start lines, keeps nothing, and has no internet permission.

## Remote

- D-pad + Select: move and open
- Up from the dock: Wallpaper / Apps / Settings
- Hold Select or press the menu button on an app: move left/right, add to or
  remove from Home, app info
- Back closes any panel; in the wallpaper picker it cancels the preview

## Rebuild

- `./build-local.sh` reproduces the included APK without Gradle (signed with
  `debug.keystore`, so updates install over the top).
- Or open in Android Studio / push to GitHub (Actions workflow included).
  Gradle builds use a different debug key: run `./uninstall.sh` once before
  installing a Gradle build over the prebuilt APK.
- Command-line Gradle needs JDK 17, Android SDK platform 34 and build-tools
  34, and Gradle 8.x (8.5 tested; Android Gradle Plugin 8.2.2 doesn't run on
  Gradle 9). There's no Gradle wrapper, so use a local Gradle:

      JAVA_HOME=<jdk-17> ANDROID_HOME=<android-sdk> gradle assembleDebug

  The APK is `app/build/outputs/apk/debug/app-debug.apk`, signed with your
  machine's `~/.android/debug.keystore`. Keep installing builds signed with the
  same key, or Android will refuse the update.

## Uninstall

    ./uninstall.sh

`uninstall.sh` sets `enabled_accessibility_services` to `null`, which also
turns off any other accessibility services. To keep them, restore the value
you noted when installing instead, then uninstall:

    adb shell settings put secure enabled_accessibility_services <saved-value>
    adb shell settings put secure accessibility_enabled <0-if-it-was-null-before>
    adb uninstall com.hector.cleanlauncher

## Known issues

- Right after leaving Netflix, Amazon's home can still show briefly on the
  first Home press: creating and first drawing the cover window is slow while
  the TV recovers memory.
- Settings → Fire TV settings is covered straight away: Fire TV's Settings
  screen belongs to the same package as Amazon's home
  (`com.amazon.tv.launcher`). Use Settings → Open Amazon home and its gear
  icon instead.
- Amazon's game hub handles the Home button itself, so leaving it falls back
  to the slower detection.

## Credits

- Design: CleanFrame by Nathan Clark (MIT).
- Photographs: Unsplash License; photographers listed in
  `app/src/main/assets/wallpapers/wallpapers.json` and shown in Settings.
- Manrope: SIL Open Font License (`assets/fonts/OFL.txt`).
- Service marks: Simple Icons (CC0) and Custom Brand Icons (MIT), via
  CleanFrame. Trademarks belong to their owners.
