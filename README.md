XenonOTA
-------
A very simple OTA checker with Android Settings look and feel.

How it works
------------
It parses the OTA xml file that you put in your file hosting and compares the version number with the local one.
If the version is newer, it notifies the user for a new ROM update.

How to use
----------
* Prepare the OTA xml file. Use this [template](https://raw.githubusercontent.com/TeamHorizon/android_packages_apps_XenonOTA/o/examples/ota_device.xml).
* Upload it to your file hosting and create a hot link of it
* Copy the [ota_conf template](https://raw.githubusercontent.com/TeamHorizon/android_packages_apps_XenonOTA/o/examples/ota_conf) to app/src/main/assets folder
  * If you are buiding this app as part of the ROM, you need to copy ota_conf in the android root folder.
  * The Android.mk will pick it up and copy it to app/src/main/assets folder automatically.
* Replace the "ota_url" with your OTA xml hot link
* Define how XenonOTA should know about the "version". The version must be parseable to a date.
  * Usually, the version is a part of a build name. For example, the 171003 in the XenonHD-171003-experimental.
* Adjust the OTA configuration according to your build name on how should XenonOTA parse the version
  * Find a key in build.prop that represents the XenonHD-171003-experimental and set it in the "version_name"
  * Set the delimiter in "version_delimiter" to "-"
  * Set the date format in "version_format" to "yyMMdd"
  * Set the position in "version_position" to "1" (zero based)
* Find a key in build.prop that represents your device name and set it in the "device_name"
  * XenonOTA will search this device name in the OTA xml file

How to build
------------
* As part of the ROM
  * In our case we generate ota_conf and ota_device.xml for each device seperately. XenonOTA is being compiled only on official builds
  * [Add this repo in your manifest](https://github.com/TeamHorizon/platform_manifest/commit/19022375e09bf21df06d91f99adadedbf93a4c1e#diff-ed2d373425288c8aef4d5b5a9603e43aR8)
  * [Include this app in the build process](https://github.com/TeamHorizon/vendor_xenonhd/blob/o/config/ota.mk#L16-L18)
  * [Generate ota_conf automatically](https://raw.githubusercontent.com/TeamHorizon/vendor_xenonhd/o/config/ota.mk)
  * [Generate ota_device.xml automatically](https://raw.githubusercontent.com/TeamHorizon/vendor_xenonhd/19f5d60fa2945f375a961d829aefaaa314825886/build/tools/ota)
* As a standalone app
  * With Android.mk: . build/envsetup.sh && breakfast device && make XenonOTA
  * With Android Studio: Import this repo to your Android Studio and build it from there
  
Credits
-------
* [Slim team](http://slimroms.net/)
  * For the original idea of the SlimCenter and app icon

Screenshots
-----------
<img alt="Screenshot"
   width="270" height="480" 
   src="https://raw.githubusercontent.com/TeamHorizon/android_packages_apps_XenonOTA/o/examples/screenshot.png" />
