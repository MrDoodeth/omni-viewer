#!/bin/sh
set -eu

work=$(mktemp -d "$BUILD_DIR/portable-linux-XXXXXX")
mkdir -p "$work/input" "$work/image" "$work/OmniViewer.AppDir" "$BUILD_DIR/dist"
cp "$BUILD_DIR"/portable-dependencies/*.jar "$work/input/"
cp "$BUILD_DIR/$APP_JAR" "$work/input/omni-viewer.jar"
"$JDK_HOME/bin/jpackage" --type app-image --name OmniViewer --icon .github/assets/icon.png --input "$work/input" --dest "$work/image" --main-jar omni-viewer.jar --main-class com.cgvsu.Main --java-options=--enable-native-access=ALL-UNNAMED
mv "$work/image/OmniViewer" "$work/OmniViewer.AppDir/opt"
cp .github/packaging/linux/OmniViewer.desktop .github/packaging/linux/AppRun "$work/OmniViewer.AppDir/"
cp .github/assets/icon.png "$work/OmniViewer.AppDir/omni-viewer.png"
chmod +x "$work/OmniViewer.AppDir/AppRun"
curl -fsSL --retry 3 https://github.com/AppImage/appimagetool/releases/download/1.9.1/appimagetool-x86_64.AppImage -o "$work/appimagetool"
printf '%s  %s\n' ed4ce84f0d9caff66f50bcca6ff6f35aae54ce8135408b3fa33abfc3cb384eb0 "$work/appimagetool" | sha256sum -c -
chmod +x "$work/appimagetool"
ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "$work/appimagetool" "$work/OmniViewer.AppDir" "$work/OmniViewer-linux-x86_64.AppImage"
mv -f "$work/OmniViewer-linux-x86_64.AppImage" "$BUILD_DIR/dist/OmniViewer-linux-x86_64.AppImage"
