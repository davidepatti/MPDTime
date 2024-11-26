#!/bin/bash

PROJECT_DIR="/Users/dpatti/IdeaProjects/MPDTime"
BUILD_DIR="$PROJECT_DIR/out/build"
PACKAGE_DIR="$PROJECT_DIR/out/package"
ICON_PATH="$PROJECT_DIR/MPDTime.icns"
MAIN_CLASS="mpdtime.Main"

# Clean and create build directories
rm -rf "$BUILD_DIR" "$PACKAGE_DIR"
mkdir -p "$BUILD_DIR" "$PACKAGE_DIR"

# Compile and package Java files
javac -d "$BUILD_DIR" "$PROJECT_DIR/src/mpdtime/"*.java

# Create manifest file
echo "Manifest-Version: 1.0" > "$BUILD_DIR/MANIFEST.MF"
echo "Main-Class: $MAIN_CLASS" >> "$BUILD_DIR/MANIFEST.MF"

# Copia delle risorse (immagini) nella directory di output
cp $PROJECT_DIR/resources/*.png "$BUILD_DIR"
#jar --create --file "$BUILD_DIR/MPDTime.jar" -C "$BUILD_DIR" .
jar --create --file "$BUILD_DIR/MPDTime.jar" --manifest "$BUILD_DIR/MANIFEST.MF" -C "$BUILD_DIR" .

# Use jpackage to create the app package
jpackage \
  --name MPDTime \
  --input "$BUILD_DIR" \
  --main-jar "MPDTime.jar" \
  --main-class "$MAIN_CLASS" \
  --type app-image \
  --icon "$ICON_PATH" \
  --resource-dir "$PROJECT_DIR/resources" \
  --dest "$PACKAGE_DIR"
