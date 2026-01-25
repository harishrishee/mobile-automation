# Mobile Automation Framework

End-to-end mobile automation framework using Appium, Cucumber (BDD), TestNG, and Allure.

## What This Framework Provides

- Appium 9 + Selenium 4
- Cucumber BDD with Page Object Model
- Android and iOS support
- Parallel device support (Android)
- Allure reports
- Reliable driver lifecycle per scenario

## Project Structure

```
mobile-automation/
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── base/          # MobileDriverManager, MobileBasePage
│   │   │   ├── hooks/         # Cucumber hooks
│   │   │   └── utils/         # ConfigReader
│   │   └── resources/
│   │       ├── apps/          # App bundles (APK/APP)
│   │       └── config/        # Platform configs (android/ios)
│   └── test/
│       ├── java/com/automation/
│       │   ├── pages/         # Page objects
│       │   ├── stepdefinitions/ # Step definitions
│       │   └── runners/       # Cucumber TestNG runner
│       └── resources/
│           ├── features/      # Gherkin feature files
│           └── allure.properties
└── pom.xml
```

## Requirements (macOS)

- Java 21+
- Maven 3.6+
- Node.js + Appium (`npm install -g appium`)
- Android SDK (for Android tests)
- Xcode (for iOS tests)

## Appium Setup

```bash
# Start Appium server
appium

# List installed drivers
appium driver list

# Install drivers
appium driver install uiautomator2
appium driver install xcuitest
```

## Configuration Files

Configs are loaded from `src/main/resources/config/{env}.properties`.

Current configs:
- `android.properties`
- `ios.properties`

Run with:
```bash
mvn test -Denv=android
mvn test -Denv=ios
```

## Android Configuration (`android.properties`)

```properties
mobile.platform=android
appium.server.url=http://localhost:4723
mobile.device.name=Android Emulator
mobile.udid=emulator-5554
mobile.platform.version=11.0

# Native app APK
mobile.app.path=src/test/resources/apps/ApiDemos-debug.apk

# Browser mode (optional)
# mobile.browser.name=Chrome

# Automation
mobile.automation.name=UiAutomator2
mobile.noReset=true
mobile.fullReset=false
```

## iOS Configuration (`ios.properties`)

Simulator build must be a `.app` compiled for `iphonesimulator`.

```properties
mobile.platform=ios
appium.server.url=http://localhost:4723

# Real device (IPA)
mobile.device.name=iPhone
# mobile.udid=YOUR_DEVICE_UDID
# mobile.platform.version=17.5

# Simulator (APP)
mobile.device.name=iPhone 17 Pro
mobile.platform.version=26.2

# App path (simulator build)
mobile.app.path=src/test/resources/apps/My Demo App.app

mobile.automation.name=XCUITest
mobile.noReset=true
mobile.fullReset=false
```

## Running Tests

```bash
# Android tests
mvn test -Denv=android -Dtags="@ApiDemos"

# iOS tests
mvn test -Denv=ios -Dtags="@iOS"
```

## Screenshots

Controlled by configuration:

```properties
screenshot.steps=false
screenshot.scenario=true
```

- `screenshot.steps=true` captures a screenshot after every step.
- `screenshot.scenario=true` captures one screenshot at scenario end (pass only).
- Failures always attach a screenshot automatically.

## Allure Categories and Trends

- Categories are defined in `src/test/resources/categories.json`.
- Trends require Allure history between runs:
  - Keep `target/allure-results/history` and copy it into the next run.
  - Example:
    ```bash
    cp -R target/allure-report/history target/allure-results/history
    ```

## Parallel Execution (Android)

Set list values in `android.properties`:

```properties
appium.server.url.list=http://localhost:4723,http://localhost:4725
mobile.device.name.list=emulator-5554,emulator-5556
mobile.udid.list=emulator-5554,emulator-5556
mobile.platform.version.list=11.0,11.0
```

Run with:
```bash
mvn test -Denv=android -DthreadCount=2
```

## Known Issues & Fixes

- **Simulator SDK mismatch**  
  Use a platform version that exists in `xcrun simctl list runtimes`.

- **Device build on Simulator**  
  `.ipa` works only on real device. Simulator requires `.app` built for `iphonesimulator`.

- **App path not found**  
  Ensure `mobile.app.path` is correct. Relative paths resolve from project root.

- **simctl not found**  
  ```bash
  xcode-select --install
  sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
  ```

## License

MIT License
<<<<<<< HEAD
# Mobile Automation Framework

End-to-end mobile automation framework using Appium, Cucumber (BDD), TestNG, and Allure.

## What This Framework Provides

- Appium 9 + Selenium 4
- Cucumber BDD with Page Object Model
- Android and iOS support
- Parallel device support (Android)
- Allure reports
- Reliable driver lifecycle per scenario

## Project Structure

```
mobile-automation/
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── base/          # MobileDriverManager, MobileBasePage
│   │   │   ├── hooks/         # Cucumber hooks
│   │   │   └── utils/         # ConfigReader
│   │   └── resources/
│   │       ├── apps/          # App bundles (APK/APP)
│   │       └── config/        # Platform configs (android/ios)
│   └── test/
│       ├── java/com/automation/
│       │   ├── pages/         # Page objects
│       │   ├── stepdefinitions/ # Step definitions
│       │   └── runners/       # Cucumber TestNG runner
│       └── resources/
│           ├── features/      # Gherkin feature files
│           └── allure.properties
└── pom.xml
```

## Requirements (macOS)

- Java 21+
- Maven 3.6+
- Node.js + Appium (`npm install -g appium`)
- Android SDK (for Android tests)
- Xcode (for iOS tests)

## Appium Setup

```bash
# Start Appium server
appium

# List installed drivers
appium driver list

# Install drivers
appium driver install uiautomator2
appium driver install xcuitest
```

## Configuration Files

Configs are loaded from `src/main/resources/config/{env}.properties`.

Current configs:
- `android.properties`
- `ios.properties`

Run with:
```bash
mvn test -Denv=android
mvn test -Denv=ios
```

## Android Configuration (android.properties)

```properties
mobile.platform=android
appium.server.url=http://localhost:4723
mobile.device.name=Android Emulator
mobile.udid=emulator-5554
mobile.platform.version=11.0

# Native app APK
mobile.app.path=src/main/resources/apps/ApiDemos-debug.apk

# Browser mode (optional)
# mobile.browser.name=Chrome

# Browser capabilities (similar to selenium-hybrid-framework)
# mobile.browser.headless=false
# mobile.browser.args=--start-maximized,--disable-notifications,--disable-infobars

# Automation
mobile.automation.name=UiAutomator2
mobile.noReset=true
mobile.fullReset=false
```

## iOS Configuration (ios.properties)

Simulator build must be a `.app` compiled for `iphonesimulator`.

```properties
mobile.platform=ios
appium.server.url=http://localhost:4723

# Real device (IPA)
mobile.device.name=iPhone
# mobile.udid=YOUR_DEVICE_UDID
# mobile.platform.version=17.5

# Simulator (APP)
mobile.device.name=iPhone 17 Pro
mobile.platform.version=26.2

# App path (simulator build)
mobile.app.path=src/main/resources/apps/My Demo App.app

mobile.automation.name=XCUITest
mobile.noReset=true
mobile.fullReset=false
```

## Running Tests

```bash
# Android tests
mvn test -Denv=android -Dtags="@ApiDemos"

# iOS tests
mvn test -Denv=ios -Dtags="@iOS"
```

## Listeners, Logging, and Screenshots

### Logging (Log4j2)

Logs are written to console via `src/main/resources/log4j2.xml`.

### Step and Scenario Screenshots

Controlled by configuration:

```properties
screenshot.steps=false
screenshot.scenario=true
```

- `screenshot.steps=true` captures a screenshot after every step.
- `screenshot.scenario=true` captures one screenshot at scenario end (pass only).
- Failures always attach a screenshot automatically.

### Allure Categories and Trends

- Categories are defined in `src/test/resources/categories.json`.
- Trends require Allure history between runs:
  - Keep `target/allure-results/history` and copy it into the next run.
  - Example:
    ```bash
    cp -R target/allure-report/history target/allure-results/history
    ```

## Writing Test Cases (Step-by-Step)

1. **Create Feature**
   - `src/test/resources/features/YourFeature.feature`
   - Add tags (e.g. `@Android`, `@iOS`)

2. **Create Page Object**
   - `src/test/java/com/automation/pages/YourPage.java`
   - Use `AppiumBy` locators
   - Keep methods small and focused

3. **Create Step Definitions**
   - `src/test/java/com/automation/stepdefinitions/YourSteps.java`
   - Bind Gherkin steps to page actions

4. **Update Runner Tags (if needed)**
   - `src/test/java/com/automation/runners/TestRunner.java`

## Android Useful Commands

```bash
adb devices
emulator -avd ApiDemos_1 -port 5554
adb -s emulator-5554 install -r /path/to/ApiDemos-debug.apk
adb -s emulator-5554 shell am start -n io.appium.android.apis/.ApiDemos
adb -s emulator-5554 shell am force-stop io.appium.android.apis
adb -s emulator-5554 shell pm clear io.appium.android.apis
```

## iOS Useful Commands

```bash
# List devices
xcrun xctrace list devices

# Boot simulator
xcrun simctl boot "iPhone 17 Pro"
open -a Simulator

# Install simulator app
xcrun simctl install booted "/path/to/My Demo App.app"
```

## Appium Inspector (Locators)

Use Appium Inspector with these minimal caps:

Android:
- `platformName=Android`
- `automationName=UiAutomator2`
- `app=/absolute/path/to/ApiDemos-debug.apk`
- `deviceName=Android Emulator`
- `udid=emulator-5554`

iOS Simulator:
- `platformName=iOS`
- `automationName=XCUITest`
- `app=/absolute/path/to/My Demo App.app`
- `deviceName=iPhone 17 Pro`
- `platformVersion=26.2`

## Parallel Execution

Set list values in `android.properties`:

```properties
appium.server.url.list=http://localhost:4723,http://localhost:4725
mobile.device.name.list=emulator-5554,emulator-5556
mobile.udid.list=emulator-5554,emulator-5556
mobile.platform.version.list=11.0,11.0
```

Run with:
```bash
mvn test -Denv=android -DthreadCount=2
```

## Known Issues & Fixes

- **Properties reload each scenario**  
  Fixed via `ConfigReader` caching (loads once per env).

- **Simulator SDK mismatch**  
  Use a platform version that exists in `xcrun simctl list runtimes`.

- **Device build on Simulator**  
  `.ipa` works only on real device. Simulator requires `.app` built for `iphonesimulator`.

- **App path not found**  
  Ensure `mobile.app.path` is correct. Relative paths resolve from project root.

- **Appium security errors**  
  Avoid `mobile: shell` unless Appium is started with `--relaxed-security`.

- **simctl not found**  
  ```bash
  xcode-select --install
  sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
  ```

## Framework Behavior

- Config loaded once per run (cached).
- Driver initialized per scenario.
- Android app reset per scenario (`terminateApp()` + `activateApp()`).

## License

MIT License
# Mobile Automation Framework

End-to-end mobile automation framework using Appium, Cucumber (BDD), TestNG, and Allure.

## What This Framework Provides

- Appium 9 + Selenium 4
- Cucumber BDD with Page Object Model
- Android and iOS support
- Parallel device support
- Allure reports
- Reliable driver lifecycle per scenario

## Project Structure

```
mobile-automation/
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── base/          # MobileDriverManager, MobileBasePage
│   │   │   ├── hooks/         # Cucumber hooks
│   │   │   └── utils/         # ConfigReader
│   │   └── resources/
│   │       ├── apps/          # App bundles (APK/APP)
│   │       └── config/        # Platform configs (android/ios)
│   └── test/
│       ├── java/com/automation/
│       │   ├── pages/         # Page objects
│       │   ├── stepdefinitions/ # Step definitions
│       │   └── runners/       # Cucumber TestNG runner
│       └── resources/
│           ├── features/      # Gherkin feature files
│           └── allure.properties
└── pom.xml
```

## Requirements (macOS)

- Java 21+
- Maven 3.6+
- Node.js + Appium (`npm install -g appium`)
- Android SDK (for Android tests)
- Xcode (for iOS tests)

## Appium Setup

```bash
# Start Appium server
appium

# List installed drivers
appium driver list

# Install drivers
appium driver install uiautomator2
appium driver install xcuitest
```

## Configuration Files

Configs are loaded from `src/main/resources/config/{env}.properties`.

Current configs:
- `android.properties`
- `ios.properties`

Run with:
```bash
mvn test -Denv=android
mvn test -Denv=ios
```

## Android Configuration (android.properties)

```properties
mobile.platform=android
appium.server.url=http://localhost:4723
mobile.device.name=Android Emulator
mobile.udid=emulator-5554
mobile.platform.version=11.0

# Native app APK
mobile.app.path=src/main/resources/apps/ApiDemos-debug.apk

# Automation
mobile.automation.name=UiAutomator2
mobile.noReset=true
mobile.fullReset=false
```

## iOS Configuration (ios.properties)

Simulator build must be a `.app` compiled for `iphonesimulator`.

```properties
mobile.platform=ios
appium.server.url=http://localhost:4723

# Real device (IPA)
mobile.device.name=iPhone
# mobile.udid=YOUR_DEVICE_UDID
# mobile.platform.version=17.5

# Simulator (APP)
mobile.device.name=iPhone 17 Pro
mobile.platform.version=26.2

# App path (simulator build)
mobile.app.path=src/main/resources/apps/My Demo App.app

mobile.automation.name=XCUITest
mobile.noReset=true
mobile.fullReset=false
```

## Running Tests

```bash
# Android tests
mvn test -Denv=android -Dtags="@ApiDemos"

# iOS tests
mvn test -Denv=ios -Dtags="@iOS"
```

## Writing Test Cases (Step-by-Step)

1. **Create Feature**
   - `src/test/resources/features/YourFeature.feature`
   - Add tags (e.g. `@Android`, `@iOS`)

2. **Create Page Object**
   - `src/test/java/com/automation/pages/YourPage.java`
   - Use `AppiumBy` locators
   - Keep methods small and focused

3. **Create Step Definitions**
   - `src/test/java/com/automation/stepdefinitions/YourSteps.java`
   - Bind Gherkin steps to page actions

4. **Update Runner Tags (if needed)**
   - `src/test/java/com/automation/runners/TestRunner.java`

## Android Useful Commands

```bash
adb devices
emulator -avd ApiDemos_1 -port 5554
adb -s emulator-5554 install -r /path/to/ApiDemos-debug.apk
adb -s emulator-5554 shell am start -n io.appium.android.apis/.ApiDemos
adb -s emulator-5554 shell am force-stop io.appium.android.apis
adb -s emulator-5554 shell pm clear io.appium.android.apis
```

## iOS Useful Commands

```bash
# List devices
xcrun xctrace list devices

# Boot simulator
xcrun simctl boot "iPhone 17 Pro"
open -a Simulator

# Install simulator app
xcrun simctl install booted "/path/to/My Demo App.app"
```

## Appium Inspector (Locators)

Use Appium Inspector with these minimal caps:

Android:
- `platformName=Android`
- `automationName=UiAutomator2`
- `app=/absolute/path/to/ApiDemos-debug.apk`
- `deviceName=Android Emulator`
- `udid=emulator-5554`

iOS Simulator:
- `platformName=iOS`
- `automationName=XCUITest`
- `app=/absolute/path/to/My Demo App.app`
- `deviceName=iPhone 17 Pro`
- `platformVersion=26.2`

## Parallel Execution

Set list values in `android.properties`:

```properties
appium.server.url.list=http://localhost:4723,http://localhost:4725
mobile.device.name.list=emulator-5554,emulator-5556
mobile.udid.list=emulator-5554,emulator-5556
mobile.platform.version.list=11.0,11.0
```

Run with:
```bash
mvn test -Denv=android -DthreadCount=2
```

## Known Issues & Fixes

- **Properties reload each scenario**  
  Fixed via `ConfigReader` caching (loads once per env).

- **Simulator SDK mismatch**  
  Use a platform version that exists in `xcrun simctl list runtimes`.

- **Device build on Simulator**  
  `.ipa` works only on real device. Simulator requires `.app` built for `iphonesimulator`.

- **App path not found**  
  Ensure `mobile.app.path` is correct. Relative paths resolve from project root.

- **Appium security errors**  
  Avoid `mobile: shell` unless Appium is started with `--relaxed-security`.

- **simctl not found**  
  ```bash
  xcode-select --install
  sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
  ```

## Framework Behavior

- Config loaded once per run (cached).
- Driver initialized per scenario.
- Android app reset per scenario (`terminateApp()` + `activateApp()`).

## License

MIT License
=======
# mobile-automation
>>>>>>> 5f1e0260bfd7833fe570dedfc432b5ba3b84a03f
