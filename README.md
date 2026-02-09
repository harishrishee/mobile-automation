# Mobile Automation Framework

End-to-end mobile automation framework using Appium, Cucumber (BDD), TestNG, and Allure reports. Supports Android and iOS, local runs, and CI via GitHub Actions. Device Farm (Appium 2 plugin) is supported for parallel/distributed device management.

## Key Libraries
- Appium Java Client (`io.appium:java-client`)
- Selenium Java (`org.seleniumhq.selenium:selenium-java`)
- Cucumber (`io.cucumber:cucumber-java`, `cucumber-testng`)
- TestNG (`org.testng:testng`)
- Allure (`io.qameta.allure:*`)
- Log4j2

## Prerequisites

### Required (Local)
- **Java 21** (project compiles with `maven.compiler.source/target=21`)
- **Maven 3.6+**
- **Node.js 18+** (20 recommended)
- **Appium 3.x**
- **Android SDK** (Android tests)
- **Xcode + iOS Simulator** (iOS tests)

### Optional (Reports)
- **Allure CLI** (for local report generation)

### Install Java 21 (macOS example)
```
brew install --cask temurin@21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

### Install Appium 3
```
npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest
```

### Device Farm Plugin (optional, for distribution)
```
appium plugin install --source=npm appium-device-farm
appium plugin list --installed
```

## Project Structure
```
mobile-automation/
├── src/
│   ├── main/
│   │   ├── java/com/automation/
│   │   │   ├── base/          # Driver manager and base page
│   │   │   ├── hooks/         # Cucumber hooks
│   │   │   └── utils/         # ConfigReader, retry utilities
│   │   └── resources/
│   │       ├── apps/          # APK/APP bundles (local)
│   │       └── config/        # android.properties, ios.properties
│   └── test/
│       ├── java/com/automation/
│       │   ├── pages/         # Page objects
│       │   ├── stepdefinitions/
│       │   └── runners/       # TestRunner
│       └── resources/
│           ├── features/      # .feature files
│           └── allure.properties
├── .github/workflows/         # GitHub Actions
└── pom.xml
```

## Configuration

### Android (`src/main/resources/config/android.properties`)
- `appium.server.url` default: `http://localhost:4723`
- `mobile.app.path` points to the APK
- Optional parallel lists:
  - `mobile.device.name.list`
  - `mobile.udid.list`
  - `mobile.platform.version.list`
  
**Core Android properties**
- `mobile.platform` (android)
- `mobile.device.name`
- `mobile.udid`
- `mobile.platform.version`
- `mobile.automation.name` (UiAutomator2)

**App selection**
- `mobile.app.path` (APK file path)
- or `mobile.app.package` + `mobile.app.activity` (installed app)
- or `mobile.browser.name=Chrome` (mobile web)

**Optional browser settings**
- `mobile.chromedriver.executable`
- `mobile.chromedriver.port`
- `mobile.browser.headless`
- `mobile.browser.args`

**Reset/Retry**
- `mobile.noReset`
- `mobile.fullReset`
- `retry.count`

### iOS (`src/main/resources/config/ios.properties`)
- `appium.server.url` default: `http://localhost:4723/wd/hub`
- `mobile.app.path` points to the `.app` (simulator build)
- WDA and simulator timeouts for CI are configurable

**Core iOS properties**
- `mobile.platform` (ios)
- `mobile.device.name`
- `mobile.udid` (optional, if you want to pin a simulator/device)
- `mobile.platform.version` (optional; leave blank for CI)
- `mobile.automation.name` (XCUITest)

**App selection**
- `mobile.app.path` (simulator `.app` build)
- or `mobile.bundle.id` (installed app)

**WDA / Simulator timeouts**
- `ios.wdaLaunchTimeout`
- `ios.wdaStartupRetries`
- `ios.wdaStartupRetryInterval`
- `ios.simulatorStartupTimeout`

**Reset/Retry**
- `mobile.noReset`
- `mobile.fullReset`
- `retry.count`

## Appium Options Mapping

The framework maps properties into Appium capabilities:

**Shared**
- `appium.server.url` → Appium server endpoint
- `mobile.device.name` → `deviceName`
- `mobile.udid` → `udid`
- `mobile.platform.version` → `platformVersion`
- `mobile.automation.name` → `automationName`
- `mobile.noReset` → `noReset`
- `mobile.fullReset` → `fullReset`

**Android specific**
- `mobile.app.path` → `app`
- `mobile.app.package` → `appPackage`
- `mobile.app.activity` → `appActivity`
- `mobile.browser.name` → `browserName`

**iOS specific**
- `mobile.app.path` → `app`
- `mobile.bundle.id` → `bundleId`
- `ios.wdaLaunchTimeout` → `wdaLaunchTimeout`
- `ios.wdaStartupRetries` → `wdaStartupRetries`
- `ios.wdaStartupRetryInterval` → `wdaStartupRetryInterval`
- `ios.simulatorStartupTimeout` → `simulatorStartupTimeout`

## Running Tests (Local)

### Start Appium (standard)
```
appium --log-level error
```

### Start Appium with Device Farm
```
appium --use-plugins=device-farm --plugin-device-farm-platform=android -pa /wd/hub
```
For iOS:
```
appium --use-plugins=device-farm --plugin-device-farm-platform=ios -pa /wd/hub
```

### Android
```
mvn test -Denv=android -Dtags="@ApiDemos"
```

### iOS
```
mvn test -Denv=ios -Dtags="@iOS"
```

## Sample Test Case
Example feature from `src/test/resources/features/ApiDemos.feature`:
```
@ApiDemos
Feature: ApiDemos App Tests
  Scenario: Navigate to Alert Dialogs
    When I open Alert Dialogs from App menu
    Then the Alert Dialogs screen should be displayed
```

## Allure Reports

Results are written to:
- `target/allure-results`

Generate report:
```
npm install -g allure-commandline
allure generate target/allure-results --clean -o target/allure-report
```

Open report:
```
open target/allure-report/index.html
```

## GitHub Actions

- Android workflow: `.github/workflows/android.yml`
- iOS workflow: `.github/workflows/ios.yml`

Allure reports are published to GitHub Pages:
- Android: `https://<user>.github.io/<repo>/<run_number>/index.html`
- iOS: `https://<user>.github.io/<repo>/ios/<run_number>/index.html`

## Parallel Execution

Parallel is supported via TestNG threads and device lists. Set:
- `threadCount` in `pom.xml`
- `mobile.device.name.list`, `mobile.udid.list`, `mobile.platform.version.list`
- start multiple Appium servers (or use Device Farm)

Example:
```
mvn test -Denv=android -DthreadCount=2
```

## Troubleshooting

- **404 from Appium**: Ensure `appium.server.url` matches your Appium base path.
  - If Appium started with `-pa /wd/hub`, use `http://localhost:4723/wd/hub`.
  - Otherwise use `http://localhost:4723`.
- **Java version error**: Ensure Java 21 is active (`java -version`).
- **iOS boot issues**: Use a supported simulator and increase timeouts in `ios.properties`.