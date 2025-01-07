# Weather

Weather app is an Android application to see current weather in your location and forecast.  

## Project Setup
1. Download the project .zip
2. Extract the .zip
3. Install and open Android Studio on your PC/Mac
4. Locate the downloaded project folder from Android Studio
5. Opn the project
6. Run the project in an emulator or a physical device over adb

## API Key Setup
1. From the project folder locate the file "gradle.properties"
2. Open the file and a new line "API_KEY = {YOUR_API}"
3. Now run the project

## How to run the project
1. Run the project on an emulator
   1. Download an emulator in Android Studio
   2. Select that emulator and run the project
2. Run the project on a physical device
   1. Enable the Developer option on your device
   2. Allow ADB from the Developer option
   3. Connect the phone via USB
   4. Select the device in Android Studio and Run the project

## Limitations 
1. Since open weather API isn't completely free, so after a certain amount of requests free limit will expire.
2. Only city name search can be found. A village or post office can't be found in this API.
3. Since this app uses free weather and forecast API. So all detailed data can't be found in the API.

## Known Issues
1. Searching with the wrong city name not responding with a proper error message.
