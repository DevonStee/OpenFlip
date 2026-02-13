#!/bin/bash
# OpenFlip Version Update Script
# Usage: ./add-version.sh <version> <title> <summary> [details]

VERSION=$1
TITLE=$2
SUMMARY=$3
DETAILS=${4:-$SUMMARY}

if [ -z "$VERSION" ] || [ -z "$TITLE" ] || [ -z "$SUMMARY" ]; then
    echo "Usage: ./add-version.sh <version> <title> <summary> [details]"
    echo "Example: ./add-version.sh \"0.7.0\" \"Widget Support\" \"[New] Added widgets\""
    exit 1
fi

VERSION_RES=$(echo $VERSION | sed 's/\./' '/g' | sed 's/-beta//' | awk -F'/' '{printf "V%d%d%d", $1, $2, $3}')

echo "Adding version $VERSION..."

sed -i.bak "s/versionName = \"[^\"]*\"/versionName = \"$VERSION\"/" app/build.gradle.kts

sed -i.bak "s/<string name=\"labelVersionValue\">v[^<]*<\/string>/<string name=\"labelVersionValue\">v$VERSION<\/string>/" app/src/main/res/values/strings.xml

NEW_VERSION_STRINGS="<!-- v$VERSION -->\n<string name=\"title${VERSION_RES}\">v$VERSION $TITLE<\/string>\n<string name=\"description${VERSION_RES}_summary\">$SUMMARY<\/string>\n<string name=\"description${VERSION_RES}_details\">$DETAILS<\/string>"

sed -i.bak "/<!-- v0\.5\.8 -->/i\\
$NEW_VERSION_STRINGS\n" app/src/main/res/values/strings.xml

NEW_VERSION_UI="        <!-- Version $VERSION -->\n        <TextView\n            android:layout_width=\"match_parent\"\n            android:layout_height=\"wrap_content\"\n            android:id=\"@+id/textInformationTitle${VERSION_RES}\"\n            android:text=\"@string/title${VERSION_RES}\"\n            android:textAppearance=\"?attr/textAppearanceTitleMedium\"\n            android:textStyle=\"bold\"\n            android:layout_marginTop=\"@dimen/spacingSmall\"\n            android:layout_marginBottom=\"@dimen/spacingMicro\" />\n\n        <TextView\n            android:layout_width=\"match_parent\"\n            android:layout_height=\"wrap_content\"\n            android:id=\"@+id/textInformationDescription${VERSION_RES}\"\n            android:text=\"@string/description${VERSION_RES}_details\"\n            android:textAppearance=\"?attr/textAppearanceBodyMedium\"\n            android:lineSpacingMultiplier=\"1.4\"\n            android:layout_marginTop=\"@dimen/spacingSmall\"\n            android:layout_marginBottom=\"@dimen/spacingLarge\" />"

sed -i.bak "/<!-- Version 0\.5\.8 -->/i\\
$NEW_VERSION_UI\n" app/src/main/res/layout/layout_settings_information.xml

rm -f app/build.gradle.kts.bak app/src/main/res/values/strings.xml.bak app/src/main/res/layout/layout_settings_information.xml.bak

echo "✅ Version $VERSION added successfully!"
echo ""
echo "⚠️  IMPORTANT: You must manually register the new TextView IDs in SettingsThemeHelper.kt"
echo "   File: app/src/main/java/com/bokehforu/openflip/ui/theme/SettingsThemeHelper.kt"
echo ""
echo "   Add to title list:"
echo "      R.id.textInformationTitle${VERSION_RES}"
echo ""
echo "   Add to description list:"
echo "      R.id.textInformationDescription${VERSION_RES}"
echo ""
echo "   Without this step, version text won't change color in white theme!"
echo ""
echo "Next step: ./gradlew :app:compileDebugKotlin"
