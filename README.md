XenonOTA
-------
An OTA updater app based on [fusionjack/slimota](https://github.com/fusionjack/slimota) with:
* Android Settings look and feel
* Themes support
* Scheduled check for updates
* Build type selector
* Gapps & Magisk downloader
* Auto-flasher

How it works
------------
It parses the OTA xml file that you put in your file hosting and compares the version number with the local one.  
If the version is newer, it notifies the user for a new ROM update.

It uses OpenGapps update checker from https://github.com/hjthjthjt/RnOpenGApps  
and Magisk update checker from https://github.com/topjohnwu/Magisk

How to use
----------
* Prepare the OTA xml file. Use this [template](https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/ota_device.xml).
* Upload it to your file hosting and create a hot link of it
* Copy the [ota_conf template](https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/ota_conf) to app/src/main/assets folder
  * If you are buiding this app as part of the ROM, you need to copy ota_conf into the android out folder.
  * The Android.mk will pick it up and copy it to app/src/main/assets folder automatically.
* Replace the "ota_experimental" or "ota_official" with your OTA xml hot link
* Define how XenonOTA should know about the "version". The version must be parseable to a date.
  * Usually, the version is a part of a build name. For example, the 171003 in the XenonHD-171003-Experimental.
* Adjust the OTA configuration according to your build name on how should XenonOTA parse the version
  * Find a key in build.prop that represents the XenonHD-171003-Experimental and set it in the "version_name"
  * Set the delimiter in "version_delimiter" to "-"
  * Set the date format in "version_format" to "yyMMdd"
  * Set the position in "version_position" to "1" (zero based)
* Find a key in build.prop that represents your device name and set it in the "device_name"
  * XenonOTA will search this device name in the OTA xml file

How to build
------------
* As part of the ROM
  * In XenonHD, ota_conf and ota_device.xml are being generated and used automatically for each device during compilation process, so you don't have to worry about anything else than setting up OTA_TYPE to Experimental/Official (XenonOTA is not being included in Unofficial builds).
  * Add this repo into your manifest  
    `<project path="packages/apps/XenonOTA" name="TeamHorizon/android_packages_apps_XenonOTA" revision="p" />`
  * Include this app in the build process  
    `PRODUCT_PACKAGES += XenonOTA`
  * [Generate ota_conf](https://raw.githubusercontent.com/TeamHorizon/vendor_xenonhd/p/config/ota.mk)
  * [Generate ota_device.xml](https://github.com/TeamHorizon/vendor_xenonhd/blob/p/build/tools/ota) (started by [build/tasks/bacon.mk](https://github.com/TeamHorizon/vendor_xenonhd/blob/p/build/tasks/bacon.mk))
* As a standalone app
  * With Android.mk: `. build/envsetup.sh && breakfast device && make XenonOTA`
  * With Android Studio: Import this repo to your Android Studio and build it from there
  
Credits
-------
* [@fusionjack](https://github.com/fusionjack)
  * For the original idea of the SlimOTA app
* [@Devil7DK](https://github.com/Devil7DK)
  * For a lot of effort to bring it to its current state
* [@dadi11](https://github.com/dadi11)
  * For the new UI concept and care on its development
* [@hjthjthjt](https://github.com/hjthjthjt)
* [@topjohnwu](https://github.com/topjohnwu)

Screenshots
-----------
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot.png" />
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot1.png" />
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot2.png" />
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot3.png" />
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot4.png" />
<img alt="Screenshot"
   width="270" height="480" 
   src="https://github.com/TeamHorizon/android_packages_apps_XenonOTA/blob/p/examples/screenshot5.png" />
