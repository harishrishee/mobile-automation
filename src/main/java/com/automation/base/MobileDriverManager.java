package com.automation.base;

import com.automation.utils.ConfigReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MobileDriverManager {
    private static final Logger logger = LogManager.getLogger(MobileDriverManager.class);
    private static ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private static final AtomicInteger DEVICE_INDEX = new AtomicInteger(0);
    private static final ConcurrentHashMap<Long, Integer> THREAD_DEVICE_INDEX = new ConcurrentHashMap<>();

    public static AppiumDriver getDriver() {
        if (driver.get() == null) {
            logger.info("Creating Appium driver");
            initializeDriver();
        }
        return driver.get();
    }

    public static void initializeDriver() {
        try {
            // Platform
            String platform = ConfigReader.getProperty("mobile.platform", "android");
            logger.info("Initializing Appium driver ({})", platform);
            
            // Initialize driver based on platform
            AppiumDriver appiumDriver;
            
            if (platform.equalsIgnoreCase("android")) {
                UiAutomator2Options options = new UiAutomator2Options();
                
                // Device configuration (supports list values for parallel runs)
                String deviceName = resolveParallelValue("mobile.device.name", "mobile.device.name.list");
                if (!deviceName.isEmpty()) {
                    options.setDeviceName(deviceName);
                }

                String udid = resolveParallelValue("mobile.udid", "mobile.udid.list");
                if (!udid.isEmpty()) {
                    options.setUdid(udid);
                }

                String platformVersion = resolveParallelValue("mobile.platform.version", "mobile.platform.version.list");
                if (!platformVersion.isEmpty()) {
                    options.setPlatformVersion(platformVersion);
                }
                
                // Automation name
                String automationName = ConfigReader.getProperty("mobile.automation.name", "UiAutomator2");
                options.setAutomationName(automationName);
                
                // Determine mode: Browser or Native App
                String browserName = ConfigReader.getProperty("mobile.browser.name", "");
                String appPath = ConfigReader.getProperty("mobile.app.path", "");
                String appPackage = ConfigReader.getProperty("mobile.app.package", "");
                String appActivity = ConfigReader.getProperty("mobile.app.activity", "");
                
                boolean isBrowserMode = !browserName.isEmpty();
                boolean isNativeAppMode = !appPath.isEmpty() || (!appPackage.isEmpty() && !appActivity.isEmpty());
                
                if (isBrowserMode) {
                    // Browser mode - Mobile Web Testing
                    System.out.println("Initializing in BROWSER mode with: " + browserName);
                    options.setCapability("browserName", browserName);
                    
                    // For Chrome browser, configure ChromeDriver for mobile Chrome
                    if (browserName.equalsIgnoreCase("Chrome")) {
                        // Check if custom ChromeDriver path is specified
                        String chromedriverExecutable = ConfigReader.getProperty("mobile.chromedriver.executable", "");
                        if (!chromedriverExecutable.isEmpty()) {
                            System.out.println("Using custom ChromeDriver path: " + chromedriverExecutable);
                            options.setCapability("chromedriverExecutable", chromedriverExecutable);
                        } else {
                            // Let Appium auto-download ChromeDriver
                            // Appium will try to download the matching ChromeDriver version
                            System.out.println("ChromeDriver will be automatically managed by Appium");
                            System.out.println("Note: If ChromeDriver download fails, you can:");
                            System.out.println("  1. Install ChromeDriver manually and set 'mobile.chromedriver.executable' in config");
                            System.out.println("  2. Use native app mode with Chrome (set mobile.app.package=com.android.chrome)");
                        }
                        
                        // Optional: Set ChromeDriver port if needed
                        String chromedriverPort = ConfigReader.getProperty("mobile.chromedriver.port", "");
                        if (!chromedriverPort.isEmpty()) {
                            options.setCapability("chromedriverPort", Integer.parseInt(chromedriverPort));
                        }

                        // Optional: Chrome args and headless mode (from config)
                        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("mobile.browser.headless", "false"));
                        String argsConfig = ConfigReader.getProperty("mobile.browser.args", "").trim();
                        List<String> args = new ArrayList<>();
                        if (headless) {
                            args.add("--headless");
                        }
                        if (!argsConfig.isEmpty()) {
                            for (String arg : argsConfig.split(",")) {
                                String trimmed = arg.trim();
                                if (!trimmed.isEmpty()) {
                                    args.add(trimmed);
                                }
                            }
                        }
                        if (!args.isEmpty()) {
                            options.setCapability("appium:chromeOptions", Map.of("args", args));
                        }
                    }
                } else if (isNativeAppMode) {
                    // Native App mode
                    System.out.println("Initializing in NATIVE APP mode");
                    
                if (!appPath.isEmpty()) {
                    // Option 1: App file path (resolve relative paths)
                    String resolvedAppPath = resolveAppPath(appPath);
                    System.out.println("Using app path: " + resolvedAppPath);
                    options.setApp(resolvedAppPath);
                    } else if (!appPackage.isEmpty() && !appActivity.isEmpty()) {
                        // Option 2: App package and activity
                        System.out.println("Using app package: " + appPackage + ", activity: " + appActivity);
                        options.setAppPackage(appPackage);
                        options.setAppActivity(appActivity);
                    }
                } else {
                    throw new RuntimeException("Either 'mobile.browser.name' or 'mobile.app.package/activity' or 'mobile.app.path' must be configured");
                }
                
                // Additional options - read from config
                boolean noReset = Boolean.parseBoolean(ConfigReader.getProperty("mobile.noReset", "true"));
                boolean fullReset = Boolean.parseBoolean(ConfigReader.getProperty("mobile.fullReset", "false"));
                options.setNoReset(noReset);
                options.setFullReset(fullReset);

                // Optional WDA settings (increase timeouts for CI)
                String wdaLaunchTimeout = ConfigReader.getProperty("ios.wdaLaunchTimeout", "").trim();
                if (!wdaLaunchTimeout.isEmpty()) {
                    options.setCapability("wdaLaunchTimeout", Integer.parseInt(wdaLaunchTimeout));
                }
                String wdaStartupRetries = ConfigReader.getProperty("ios.wdaStartupRetries", "").trim();
                if (!wdaStartupRetries.isEmpty()) {
                    options.setCapability("wdaStartupRetries", Integer.parseInt(wdaStartupRetries));
                }
                String wdaStartupRetryInterval = ConfigReader.getProperty("ios.wdaStartupRetryInterval", "").trim();
                if (!wdaStartupRetryInterval.isEmpty()) {
                    options.setCapability("wdaStartupRetryInterval", Integer.parseInt(wdaStartupRetryInterval));
                }
                String simulatorStartupTimeout = ConfigReader.getProperty("ios.simulatorStartupTimeout", "").trim();
                if (!simulatorStartupTimeout.isEmpty()) {
                    options.setCapability("simulatorStartupTimeout", Integer.parseInt(simulatorStartupTimeout));
                }
                
                URL url = new URL(resolveParallelValue("appium.server.url", "appium.server.url.list"));
                appiumDriver = new AndroidDriver(url, options);
                
            } else if (platform.equalsIgnoreCase("ios")) {
                XCUITestOptions options = new XCUITestOptions();
                
                // Device configuration (supports list values for parallel runs)
                String deviceName = resolveParallelValue("mobile.device.name", "mobile.device.name.list");
                if (!deviceName.isEmpty()) {
                    options.setDeviceName(deviceName);
                }

                String udid = resolveParallelValue("mobile.udid", "mobile.udid.list");
                if (!udid.isEmpty()) {
                    options.setUdid(udid);
                }

                String platformVersion = resolveParallelValue("mobile.platform.version", "mobile.platform.version.list");
                if (!platformVersion.isEmpty()) {
                    options.setPlatformVersion(platformVersion);
                }
                
                // Automation name
                options.setAutomationName("XCUITest");
                
                // App configuration
                String appPath = ConfigReader.getProperty("mobile.app.path", "");
                if (!appPath.isEmpty()) {
                    String resolvedAppPath = resolveAppPath(appPath);
                    options.setApp(resolvedAppPath);
                } else {
                    String bundleId = ConfigReader.getProperty("mobile.bundle.id", "");
                    if (!bundleId.isEmpty()) {
                        options.setBundleId(bundleId);
                    } else {
                        String browserName = ConfigReader.getProperty("mobile.browser.name", "");
                        if (!browserName.isEmpty()) {
                            // For mobile web, set browser name via capabilities
                            options.setCapability("browserName", browserName);
                        }
                    }
                }
                
                // Additional options - read from config
                boolean noReset = Boolean.parseBoolean(ConfigReader.getProperty("mobile.noReset", "true"));
                boolean fullReset = Boolean.parseBoolean(ConfigReader.getProperty("mobile.fullReset", "false"));
                options.setNoReset(noReset);
                options.setFullReset(fullReset);
                
                URL url = new URL(resolveParallelValue("appium.server.url", "appium.server.url.list"));
                appiumDriver = new IOSDriver(url, options);
            } else {
                throw new RuntimeException("Unsupported platform: " + platform);
            }
            
            appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.set(appiumDriver);
            logger.info("Appium driver initialized");
            
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            logger.info("Quitting Appium driver");
            driver.get().quit();
            driver.remove();
        }
    }

    private static String resolveParallelValue(String singleKey, String listKey) {
        String listValue = ConfigReader.getProperty(listKey, "").trim();
        if (!listValue.isEmpty()) {
            List<String> values = Arrays.stream(listValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            if (!values.isEmpty()) {
                int index = getDeviceIndex(values.size());
                return values.get(index);
            }
        }
        return ConfigReader.getProperty(singleKey, "");
    }

    private static int getDeviceIndex(int size) {
        long threadId = Thread.currentThread().getId();
        return THREAD_DEVICE_INDEX.computeIfAbsent(threadId,
                key -> DEVICE_INDEX.getAndIncrement() % size);
    }

    private static String resolveAppPath(String appPath) {
        Path path = Paths.get(appPath);
        if (!path.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            Path projectRelative = Paths.get(projectRoot).resolve(appPath).normalize();
            if (Files.exists(projectRelative)) {
                return projectRelative.toString();
            }
        }

        if (Files.exists(path)) {
            return path.toString();
        }

        String normalized = appPath.startsWith("/") ? appPath.substring(1) : appPath;
        URL resourceUrl = MobileDriverManager.class.getClassLoader().getResource(normalized);
        if (resourceUrl != null) {
            return Paths.get(resourceUrl.getPath()).toString();
        }

        throw new RuntimeException("App file not found at path: " + appPath +
                ". Update mobile.app.path to an absolute path or ensure the file exists.");
    }
}
