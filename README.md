# 🚪 Door Frame Detector

Android app for real-time door frame detection using AR and machine learning.

## 📱 Features
- **Real-time ground detection** using ARCore
- **Door frame recognition** using Google ML Kit
- **AR visualization** with 3D markers
- **Centimeter-level accuracy** positioning

## 🛠️ Tech Stack
- **AR Framework**: ARCore + Sceneform
- **Object Detection**: Google ML Kit Object Detection
- **Real-time Processing**: CameraX + Kotlin coroutines
- **3D Rendering**: Sceneform UX & Assets

## 🔧 Installation & Building

### Prerequisites
- Android Studio 2022+
- Android SDK 34+
- Java/Kotlin development environment

### Manual Build
```bash
# Clone the repository
git clone https://github.com/tulay-hub/DoorFrameDetector.git

# Build the APK
./gradlew assembleRelease

# APK will be at:
# app/build/outputs/apk/release/app-release.apk
```

### GitHub Actions (Auto-Build)
This repository includes GitHub Actions configuration that automatically:
1. Builds release APK on every push to main
2. Uploads APK as workflow artifact
3. Creates GitHub release if configured

To download pre-built APK:
1. Go to **Actions** tab
2. Select the latest workflow run
3. Download `door-frame-detector-apk` artifact

## 📁 Project Structure
```
DoorFrameDetector/
├── app/
│   ├── src/main/java/com/example/doorframedetector/
│   │   ├── MainActivity.kt          # Main UI + AR integration
│   │   ├── VisionProcessor.kt       # ML door frame detection
│   │   └── CameraController.kt      # Camera real-time processing
│   ├── src/main/res/
│   │   ├── layout/activity_main.xml
│   │   ├── values/strings.xml
│   │   └── drawable/ (app icons)
│   ├── build.gradle                 # Dependencies and config
│   └── AndroidManifest.xml          # Permissions
├── .github/workflows/
│   └── build-apk.yml                # GitHub Actions config
├── build.gradle
├── settings.gradle
└── gradlew.bat                      # Windows build script
```

## 📋 Requirements
- **Android 8.0+** (API 26+)
- **ARCore supported device** (most phones after 2018)
- **Camera permissions** for real-time detection

## 🚀 How to Use
1. Install the APK on your device
2. Grant camera permissions when prompted
3. Point camera at a door frame
4. App will detect ground plane (green status)
5. Then detect door frame (orange status)
6. AR marker will appear (yellow transparent cube)

## 🔧 Troubleshooting
- **ARCore not supported**: Update Google Play Services
- **Camera not starting**: Check permissions, restart phone
- **Detection unstable**: Ensure good lighting, move closer

## 📞 Support
For issues:
1. Open an issue in the GitHub repository
2. Include error screenshots
3. Mention your device model
4. Describe the problem in detail

## 📄 License
This project is available under the MIT License.# 🚪 Door Frame Detector

Android app for real-time door frame detection using AR and machine learning.

## 📱 Features
- **Real-time ground detection** using ARCore
- **Door frame recognition** using Google ML Kit
- **AR visualization** with 3D markers
- **Centimeter-level accuracy** positioning

## 🛠️ Tech Stack
- **AR Framework**: ARCore + Sceneform
- **Object Detection**: Google ML Kit Object Detection
- **Real-time Processing**: CameraX + Kotlin coroutines
- **3D Rendering**: Sceneform UX & Assets

## 🔧 Installation & Building

### Prerequisites
- Android Studio 2022+
- Android SDK 34+
- Java/Kotlin development environment

### Manual Build
```bash
# Clone the repository
git clone https://github.com/tulay-hub/DoorFrameDetector.git

# Build the APK
./gradlew assembleRelease

# APK will be at:
# app/build/outputs/apk/release/app-release.apk
```

### GitHub Actions (Auto-Build)
This repository includes GitHub Actions configuration that automatically:
1. Builds release APK on every push to main
2. Uploads APK as workflow artifact
3. Creates GitHub release if configured

To download pre-built APK:
1. Go to **Actions** tab
2. Select the latest workflow run
3. Download `door-frame-detector-apk` artifact

## 📁 Project Structure
```
DoorFrameDetector/
├── app/
│   ├── src/main/java/com/example/doorframedetector/
│   │   ├── MainActivity.kt          # Main UI + AR integration
│   │   ├── VisionProcessor.kt       # ML door frame detection
│   │   └── CameraController.kt      # Camera real-time processing
│   ├── src/main/res/
│   │   ├── layout/activity_main.xml
│   │   ├── values/strings.xml
│   │   └── drawable/ (app icons)
│   ├── build.gradle                 # Dependencies and config
│   └── AndroidManifest.xml          # Permissions
├── .github/workflows/
│   └── build-apk.yml                # GitHub Actions config
├── build.gradle
├── settings.gradle
└── gradlew.bat                      # Windows build script
```

## 📋 Requirements
- **Android 8.0+** (API 26+)
- **ARCore supported device** (most phones after 2018)
- **Camera permissions** for real-time detection

## 🚀 How to Use
1. Install the APK on your device
2. Grant camera permissions when prompted
3. Point camera at a door frame
4. App will detect ground plane (green status)
5. Then detect door frame (orange status)
6. AR marker will appear (yellow transparent cube)

## 🔧 Troubleshooting
- **ARCore not supported**: Update Google Play Services
- **Camera not starting**: Check permissions, restart phone
- **Detection unstable**: Ensure good lighting, move closer

## 📞 Support
For issues:
1. Open an issue in the GitHub repository
2. Include error screenshots
3. Mention your device model
4. Describe the problem in detail

## 📄 License
This project is available under the MIT License.