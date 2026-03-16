# eIDRomania Android SDK — Example Application

Demonstrates how to integrate the **eIDRomania Android SDK** into an Android application to read Romanian electronic identity cards (CEI) via NFC.

## What this example shows

- Initializing the SDK with a license key
- NFC foreground dispatch setup to capture eID tags
- Reading a card with **CAN + PIN** — retrieves full data including name, CNP, address, face photo and signature
- Progress callbacks during reading
- Typed error handling (wrong CAN, wrong PIN, card locked, etc.)
- Displaying card data with Jetpack Compose

> **Tested on:** Physical Romanian eID card (CEI) on Android 14+

## Prerequisites

| Requirement | Notes |
|-------------|-------|
| Android 9.0+ (API 28) | `minSdk = 28` |
| NFC-capable device | Required — no emulation available |
| Romanian eID card (CEI) | Physical card required |
| eIDRomania SDK license key | Contact [office@up2date.ro](mailto:office@up2date.ro) |

## Setup

### 1. Add your license key

Edit `app/src/main/java/com/example/eid/EIDExampleApplication.kt` and replace `LICENSE_KEY`:

```kotlin
private const val LICENSE_KEY = "YOUR_LICENSE_KEY_HERE"
```

The license key is a JWT issued for your app's package name. Contact [office@up2date.ro](mailto:office@up2date.ro) to obtain one.

## Build & Run

Open in Android Studio and run on a physical device with NFC. The app does not work on an emulator.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Project structure

```
app/src/main/java/com/example/eid/
├── EIDExampleApplication.kt   # SDK initialization with license key
├── EIDReaderViewModel.kt      # Reading logic, NFC tag handling, UI state
├── LicenseHelper.kt           # License status logging utility
├── MainActivity.kt            # NFC foreground dispatch + Compose UI
└── ui/
    ├── components/
    │   └── CardDataDisplay.kt # Card data display composable
    └── theme/                 # Material theme
app/build.gradle.kts           # App module with SDK dependency
settings.gradle.kts            # Artifact Registry repository config
```

## SDK dependency

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://europe-west1-maven.pkg.dev/eid-romania/eid-romania-sdk")
            credentials {
                username = "_json_key_base64"
                password = "YOUR_SDK_KEY_HERE" // provided by Up2Date Software SRL with your license
            }
            authentication { create<BasicAuthentication>("basic") }
        }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.up2date.eidromania:eidromania-android-sdk:1.0.5")
}
```

## Error handling reference

| Error | Meaning | Action |
|-------|---------|--------|
| `InvalidCAN` | Wrong 6-digit CAN | Check CAN printed on card front |
| `InvalidPIN` | Wrong PIN (`attemptsRemaining` available) | Check PIN; stop at 1 remaining |
| `CardLocked` | Too many wrong PINs | Card owner must visit DEP office |
| `TagLost` | Card moved during reading | Keep card still; retry |
| `Timeout` | Communication timeout | Retry; check NFC distance |

## License

This example application is provided by **Up2Date Software SRL** for integration reference.
The SDK itself requires a separate commercial license — contact [office@up2date.ro](mailto:office@up2date.ro).
