package com.automation.hooks;

import com.automation.base.MobileDriverManager;
import com.automation.utils.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

public class Hooks {
    private static final Logger logger = LogManager.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {
        String env = System.getProperty("env");
        if (env == null || env.trim().isEmpty()) {
            env = "ios"; // Default to ios if not specified
        }
        ConfigReader.loadProperties(env);
        MobileDriverManager.initializeDriver();
        relaunchAppForScenario();
        logger.info("Started scenario: {}", scenario.getName());
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (isScreenshotStepsEnabled()) {
            attachScreenshot("Step Screenshot", scenario);
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            // Take screenshot on failure
            try {
                byte[] screenshot = ((TakesScreenshot) MobileDriverManager.getDriver())
                        .getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Screenshot");
            } catch (Exception e) {
                logger.warn("Failed to take screenshot: {}", e.getMessage());
            }
        }
        if (isScreenshotScenarioEnabled() && !scenario.isFailed()) {
            attachScreenshot("Scenario Screenshot", scenario);
        }
        closeAppAfterScenario();
        resetAppToFreshState();
        MobileDriverManager.quitDriver();
        logger.info("Finished scenario: {} (status: {})", scenario.getName(), scenario.getStatus());

    }

    private void resetAppToFreshState() {
        AppiumDriver driver = MobileDriverManager.getDriver();
        if (!(driver instanceof AndroidDriver)) {
            return;
        }

        AndroidDriver androidDriver = (AndroidDriver) driver;
        String appPackage = resolveAppPackage(androidDriver);

        try {
            androidDriver.terminateApp(appPackage);
            Thread.sleep(1000);
            androidDriver.activateApp(appPackage);
        } catch (Exception e) {
            logger.warn("Failed to reset app state: {}", e.getMessage());
        }
    }

    private String resolveAppPackage(AndroidDriver androidDriver) {
        Object appPackageCap = androidDriver.getCapabilities().getCapability("appPackage");
        if (appPackageCap != null && !appPackageCap.toString().isBlank()) {
            return appPackageCap.toString();
        }
        try {
            String currentPackage = androidDriver.getCurrentPackage();
            if (currentPackage != null && !currentPackage.isBlank()) {
                return currentPackage;
            }
        } catch (Exception ignored) {
            // fall back to config
        }
        return ConfigReader.getProperty("mobile.app.package", "io.appium.android.apis");
    }

    private boolean isScreenshotStepsEnabled() {
        return Boolean.parseBoolean(ConfigReader.getProperty("screenshot.steps", "false"));
    }

    private boolean isScreenshotScenarioEnabled() {
        return Boolean.parseBoolean(ConfigReader.getProperty("screenshot.scenario", "false"));
    }

    private void attachScreenshot(String name, Scenario scenario) {
        try {
            AppiumDriver driver = MobileDriverManager.getDriver();
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", name);
            }
        } catch (Exception e) {
            logger.warn("Failed to attach screenshot: {}", e.getMessage());
        }
    }

    private void relaunchAppForScenario() {
        try {
            AppiumDriver driver = MobileDriverManager.getDriver();
            boolean relaunched = true;
            if (driver instanceof AndroidDriver) {
                relaunched = relaunchAndroidApp((AndroidDriver) driver);
            } else if (driver instanceof IOSDriver) {
                relaunched = relaunchIosApp((IOSDriver) driver);
            } else {
                relaunched = false;
            }
            if (!relaunched) {
                MobileDriverManager.quitDriver();
                MobileDriverManager.initializeDriver();
            }
        } catch (Exception e) {
            logger.warn("Failed to relaunch app: {}", e.getMessage());
        }
    }

    private boolean relaunchAndroidApp(AndroidDriver androidDriver) {
        try {
            String appPackage = resolveAppPackage(androidDriver);
            if (appPackage == null || appPackage.isBlank()) {
                return false;
            }
            boolean terminated = androidDriver.terminateApp(appPackage);
            Thread.sleep(1000);
            if (!terminated) {
                return false;
            }
            androidDriver.activateApp(appPackage);
            return true;
        } catch (Exception e) {
            throw new WebDriverException("Failed to relaunch Android app", e);
        }
    }

    private boolean relaunchIosApp(IOSDriver driver) {
        try {
            String bundleId = resolveIosBundleId(driver);
            if (bundleId == null || bundleId.isBlank()) {
                return false;
            }
            boolean terminated = driver.terminateApp(bundleId);
            Thread.sleep(1000);
            if (!terminated) {
                return false;
            }
            driver.activateApp(bundleId);
            return true;
        } catch (Exception e) {
            throw new WebDriverException("Failed to relaunch iOS app", e);
        }
    }

    private String resolveIosBundleId(AppiumDriver driver) {
        Object bundleIdCap = driver.getCapabilities().getCapability("bundleId");
        if (bundleIdCap == null) {
            bundleIdCap = driver.getCapabilities().getCapability("appium:bundleId");
        }
        if (bundleIdCap != null && !bundleIdCap.toString().isBlank()) {
            return bundleIdCap.toString();
        }
        String configBundleId = ConfigReader.getProperty("mobile.bundle.id", "");
        if (!configBundleId.isBlank()) {
            return configBundleId;
        }
        return null;
    }

    private void closeAppAfterScenario() {
        try {
            AppiumDriver driver = MobileDriverManager.getDriver();
            if (driver instanceof IOSDriver) {
                String bundleId = resolveIosBundleId(driver);
                if (bundleId != null && !bundleId.isBlank()) {
                    ((IOSDriver) driver).terminateApp(bundleId);
                }
            } else if (driver instanceof AndroidDriver) {
                String appPackage = resolveAppPackage((AndroidDriver) driver);
                if (appPackage != null && !appPackage.isBlank()) {
                    ((AndroidDriver) driver).terminateApp(appPackage);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to close app: {}", e.getMessage());
        }
    }
}
