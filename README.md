# Go Ubiquitous (Android Ubiquitous computing)

##Overview

Sunshine's mobile app (used in the Udacity Advanced Android course) synchronizes weather information from OpenWeatherMap on Android Phones and Tablets. **Go Ubiquitous** project is used to design a watch face for Android Wear so that users can access Sunshine's weather information at a glance.
 
![device-2016-04-18-224606](https://cloud.githubusercontent.com/assets/15085932/14612956/8415aee0-05b7-11e6-80fc-9fbb3318dc9c.png)
![preview_digital_circular](https://cloud.githubusercontent.com/assets/15085932/14612954/84137648-05b7-11e6-8e7a-51cad4a84ea2.png)
![device-2016-04-18-224518](https://cloud.githubusercontent.com/assets/15085932/14612955/84147f3e-05b7-11e6-8fe4-c5d0e4a1d33b.png)

##Prerequisites

 The app source code is provided in the course [repository] (https://www.udacity.com/api/nodes/4292653440/supplemental_media/xyzreaderzip/download)
 
* The app is built with compileSdkVersion 23 and requires [JDK 7](http://oracle.com/technetwork/java/javase/downloads/index.html) or higher

* Android Studio

* Update your SDK tools to version 23.0.0 or higher 

* Update your SDK with Android 4.4W.2 (API 20) or higher 

* Android wear or emulator(instructions to configure which are given below)

* The [Wearable Support Library](http://developer.android.com/intl/es/reference/android/support/wearable/watchface/package-summary.html) provides the necessary classes that you extend to create watch face implementations. The Google Play services client libraries (play-services and play-services-wearable) are required to sync data items between the companion device and the wearable with the [Wearable Data Layer API](http://developer.android.com/intl/es/training/wearables/data-layer/index.html).

##Instructions

###Get the source codes

Get the source code of the library and example app, by cloning git repository or downloading archives.

 * If you use **git**, execute the following command in your workspace directory.
 
    `$ git clone https://github.com/Ruchita7/sunshine_watch_face.git`
    
* If you are using Windows, try it on GitBash or Cygwin or something that supports git.
 
###Import the project to Android Studio
 
Once the project is cloned to disk you can import into Android Studio:

 * From the toolbar select **File > Import Project**, or Import Non-Android Studio project from the Welcome Quick Start.

 *  Select the directory that is cloned. If you can't see your cloned directory, click "Refresh" icon and find it.

 *  Android Studio will import the project and build it. This might take minutes to complete. Even when the project window is opened, wait until the Gradle tasks are finished and indexed.
 
###Build and install using Gradle

If you just want to install the app to your device, you don't have to import project to Android Studio.

 •  After cloning the project, make sure **ANDROID_HOME** environment variable is set to point to your Android SDK. See [Getting Started with Gradle](https://guides.codepath.com/android/Getting-Started-with-Gradle).

 •  Connect an Android device to your computer or start an Android emulator.

 •  Compile the sample and install it. Run gradlew installDebug. Or if you on a Windows computer, use **gradlew.bat** instead.
 
###Set Up an Android Wear Emulator or Device

It is recommended that you develop on real hardware so you can better gauge the user experience. However, the emulator lets you test out different types of screen shapes, which is useful for testing.To set up an Android Wear Emulator or Device follow [these instructions](http://developer.android.com/intl/es/training/wearables/apps/creating.html#SetupEmulator)


###Contributing

Please follow the **"fork-and-pull"** Git workflow while contributing to this project

 **Fork** the repo on GitHub

 **Commit** changes to a branch in your fork

 **Pull request "upstream"** with your changes

 **Merge** changes in to "upstream" repo

**NOTE:** Be sure to merge the latest from **"upstream"** before making a pull request!
 

