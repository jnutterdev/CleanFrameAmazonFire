#!/usr/bin/env bash
# Gradle-free build used to produce the included APK.
set -euo pipefail
T=/tmp/tools; BT=$T/build-tools/34.0.4; AJ=$T/android.jar; JDK=/tmp/jdk/bin
P=$(cd "$(dirname "$0")" && pwd); M=$P/app/src/main; O=$P/out
rm -rf "$O"; mkdir -p "$O"/{gen,classes,dex}
sed 's#<manifest #<manifest package="com.hector.cleanlauncher" #' "$M/AndroidManifest.xml" > "$O/AndroidManifest.xml"
"$BT/aapt2" compile --dir "$M/res" -o "$O/res.zip"
"$BT/aapt2" link -o "$O/base.apk" -I "$AJ" --manifest "$O/AndroidManifest.xml" \
  --min-sdk-version 22 --target-sdk-version 34 --version-code 1 --version-name 1.0 \
  -A "$M/assets" -0 jpg --java "$O/gen" "$O/res.zip"
"$JDK/javac" -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$AJ:$BT/core-lambda-stubs.jar" -d "$O/classes" \
  $(find "$M/java" "$O/gen" -name '*.java')
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 22 --lib "$AJ" \
  --output "$O/dex" $(find "$O/classes" -name '*.class')
cp "$O/base.apk" "$O/unsigned.apk"; (cd "$O/dex" && zip -q -j "$O/unsigned.apk" classes.dex)
"$BT/zipalign" -f -p 4 "$O/unsigned.apk" "$O/aligned.apk"
[ -f "$P/debug.keystore" ] || "$JDK/keytool" -genkeypair -keystore "$P/debug.keystore" -storepass android \
  -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US" >/dev/null 2>&1
java -jar "$BT/lib/apksigner.jar" sign --ks "$P/debug.keystore" --ks-pass pass:android \
  --key-pass pass:android --out "$P/CleanLauncher.apk" "$O/aligned.apk"
java -jar "$BT/lib/apksigner.jar" verify "$P/CleanLauncher.apk" && echo "BUILD OK: $(du -h "$P/CleanLauncher.apk" | cut -f1)"
