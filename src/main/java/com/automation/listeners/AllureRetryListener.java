package com.automation.listeners;

import com.automation.base.MobileDriverManager;
import com.automation.utils.ConfigReader;
import com.automation.utils.RetryAnalyzer;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AllureRetryListener tracks retry attempts and properly displays them in Allure reports
 * Uses both IInvokedMethodListener and ITestListener to properly track retries
 * Also uses Allure's lifecycle hooks to ensure historyId is set correctly
 */
public class AllureRetryListener implements IInvokedMethodListener, ITestListener {
    private static final Logger logger = LogManager.getLogger(AllureRetryListener.class);
    private static final ThreadLocal<Integer> retryCount = new ThreadLocal<>();
    
    // Store original historyId for each test method to ensure retries share the same ID
    private static final ConcurrentMap<String, String> testHistoryIds = new ConcurrentHashMap<>();
    
    static {
        logger.debug("AllureRetryListener initialized");
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // Track retry count before method invocation
        // Use static method to get retry count for this test
        int currentRetry = RetryAnalyzer.getRetryCountForTest(testResult);
        retryCount.set(currentRetry);
        logger.debug("beforeInvocation retry={} test={}", currentRetry, testResult.getMethod().getMethodName());
        
        // Try to set historyId here as well
        if (currentRetry > 0) {
            String testKey = getTestKey(testResult);
            String originalHistoryId = testHistoryIds.get(testKey);
            if (originalHistoryId != null) {
                logger.debug("beforeInvocation set historyId={} retry={}", originalHistoryId, currentRetry);
                AllureLifecycle lifecycle = Allure.getLifecycle();
                String uuid = lifecycle.getCurrentTestCase().orElse(null);
                if (uuid != null) {
                    lifecycle.updateTestCase(uuid, tr -> {
                        tr.setHistoryId(originalHistoryId);
                        logger.debug("beforeInvocation updated historyId={}", originalHistoryId);
                    });
                } else {
                    logger.warn("beforeInvocation no UUID available");
                }
            }
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        // This is called after Allure has started the test case
        // Get retry count from ThreadLocal or static method
        int retryCountValue = RetryAnalyzer.getRetryCountForTest(result);
        Integer threadLocalRetry = retryCount.get();
        if (threadLocalRetry != null && threadLocalRetry > retryCountValue) {
            retryCountValue = threadLocalRetry;
        }
        
        // Make final for use in lambdas
        final int currentRetry = retryCountValue;
        
        logger.debug("onTestStart retry={} test={}", currentRetry, result.getMethod().getMethodName());
        
        if (currentRetry > 0 || result.getMethod().getRetryAnalyzer(result) != null) {
            String testKey = getTestKey(result);
            AllureLifecycle lifecycle = Allure.getLifecycle();
            
            // Get current test case UUID - try multiple times as it might not be immediately available
            String currentUuid = null;
            for (int i = 0; i < 10 && currentUuid == null; i++) {
                currentUuid = lifecycle.getCurrentTestCase().orElse(null);
                if (currentUuid == null) {
                    try {
                        Thread.sleep(50); // Wait a bit for Allure to initialize
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Make final for use in lambdas
            final String finalUuid = currentUuid;
            
            if (finalUuid != null) {
                if (currentRetry > 0) {
                    // This is a retry - reuse the original historyId
                    String originalHistoryId = testHistoryIds.get(testKey);
                    if (originalHistoryId != null) {
                        logger.info("Retry attempt {} for {}", currentRetry, result.getMethod().getMethodName());
                        
                        // Make final for lambda
                        final String finalHistoryId = originalHistoryId;
                        
                        // Update the test result with the same historyId
                        lifecycle.updateTestCase(finalUuid, testResult -> {
                            // CRITICAL: Set the same historyId as the original test
                            testResult.setHistoryId(finalHistoryId);
                            logger.debug("Set historyId to: {} for retry attempt {}", finalHistoryId, currentRetry);
                        });
                        
                        // Also try to update it again after a short delay to ensure it's set
                        new Thread(() -> {
                            try {
                                Thread.sleep(100);
                                lifecycle.updateTestCase(finalUuid, tr -> {
                                    if (!finalHistoryId.equals(tr.getHistoryId())) {
                                        tr.setHistoryId(finalHistoryId);
                                        logger.debug("Updated historyId in delayed update");
                                    }
                                });
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        logger.warn("Original historyId not found for {}", testKey);
                        // Try to generate and store it
                        lifecycle.updateTestCase(currentUuid, testResult -> {
                            String historyId = testResult.getHistoryId();
                            if (historyId == null || historyId.isEmpty()) {
                                historyId = generateHistoryId(result);
                            }
                            testHistoryIds.put(testKey, historyId);
                            testResult.setHistoryId(historyId);
                            logger.debug("Stored historyId {} for {}", historyId, testKey);
                        });
                    }
                    
                    // Add retry labels and parameters
                    Allure.label("retry_attempt", String.valueOf(currentRetry));
                    Allure.parameter("Retry Attempt", String.valueOf(currentRetry));
                    Allure.step("Retry attempt " + currentRetry);
                } else {
                    // First attempt - capture and store the historyId
                    lifecycle.updateTestCase(finalUuid, testResult -> {
                        String historyId = testResult.getHistoryId();
                        if (historyId == null || historyId.isEmpty()) {
                            // Generate one based on test method
                            historyId = generateHistoryId(result);
                            testResult.setHistoryId(historyId);
                            logger.debug("Generated historyId {} for {}", historyId, testKey);
                        } else {
                            logger.debug("Found historyId {} for {}", historyId, testKey);
                        }
                        // Store it for retries
                        testHistoryIds.put(testKey, historyId);
                        logger.debug("Stored historyId {} for {}", historyId, testKey);
                    });
                }
            } else {
                logger.warn("No current test case UUID for {}", testKey);
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // Clean up after test completion
        if (testResult.getStatus() == ITestResult.SUCCESS || 
            testResult.getStatus() == ITestResult.FAILURE) {
            Integer retry = retryCount.get();
            if (retry != null && retry > 0) {
                logger.info("Test completed on retry {}", retry);
            }
            // Don't remove retryCount here - keep it for potential cleanup later
        }
    }

    /**
     * Generate a consistent historyId for a test method
     * This should match Allure's default generation logic
     */
    private String generateHistoryId(ITestResult result) {
        // Generate historyId based on test class, method name, and parameters
        // This should be consistent across retries
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        String paramsStr = params != null ? java.util.Arrays.toString(params) : "";
        String deviceId = getDeviceIdentifier();
        
        // Create a hash-based historyId similar to Allure's default
        String baseId = className + "." + methodName + paramsStr + "|" + deviceId;
        // Use a simple hash to create a consistent ID
        int hashCode = baseId.hashCode();
        String historyId = String.valueOf(Math.abs(hashCode));
        
        logger.debug("Generated historyId: {} for test: {}.{} device={}", historyId, className, methodName, deviceId);
        return historyId;
    }

    /**
     * Get unique key for test method to track historyId
     */
    private String getTestKey(ITestResult result) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        String paramsStr = params != null ? java.util.Arrays.toString(params) : "";
        return className + "." + methodName + "(" + paramsStr + ")@" + getDeviceIdentifier();
    }

    private String getDeviceIdentifier() {
        try {
            AppiumDriver driver = MobileDriverManager.getDriver();
            if (driver != null && driver.getCapabilities() != null) {
                Object udid = driver.getCapabilities().getCapability("udid");
                if (udid != null && !udid.toString().isEmpty()) {
                    return udid.toString();
                }
                Object deviceName = driver.getCapabilities().getCapability("deviceName");
                if (deviceName != null && !deviceName.toString().isEmpty()) {
                    return deviceName.toString();
                }
            }
        } catch (Exception ignored) {
        }
        String udid = ConfigReader.getProperty("mobile.udid", "").trim();
        if (!udid.isEmpty()) {
            return udid;
        }
        return ConfigReader.getProperty("mobile.device.name", "unknown-device").trim();
    }

    /**
     * Get current retry count for the test
     */
    public static int getCurrentRetryCount() {
        // First try ThreadLocal
        Integer retry = retryCount.get();
        if (retry != null) {
            return retry;
        }
        // Fall back to RetryAnalyzer's ThreadLocal
        return RetryAnalyzer.getCurrentThreadRetryCount();
    }
}
