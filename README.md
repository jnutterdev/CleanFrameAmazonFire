# Clean Launcher for Fire TV

A quiet home screen for the Fire TV, modelled on
[CleanFrame](https://github.com/nthnclrk/CleanFrame) (Samsung The Frame / Tizen)
and also rebuilt as a native Android app.

A photograph, the time, and a frosted-glass dock of your apps.

## Install

    ./install.sh                 # defaults to 192.168.50.231
    ./install.sh 192.168.x.x     # another TV

`CleanLauncher.apk` is prebuilt. The script installs it and enables the
accessibility service that sends the Home button here (Fire OS can't change
the default launcher).

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

## Uninstall

    ./uninstall.sh

## Credits

- Design: CleanFrame by Nathan Clark (MIT).
- Photographs: Unsplash License; photographers listed in
  `app/src/main/assets/wallpapers/wallpapers.json` and shown in Settings.
- Manrope: SIL Open Font License (`assets/fonts/OFL.txt`).
- Service marks: Simple Icons (CC0) and Custom Brand Icons (MIT), via
  CleanFrame. Trademarks belong to their owners.
