package com.automation.listeners;

import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allure Extension to handle retries by ensuring consistent historyId
 * This extension intercepts test result creation via Allure's extension mechanism
 */
public class AllureRetryExtension {
    private static final Logger logger = LogManager.getLogger(AllureRetryExtension.class);
    
    // Store original historyId for each test to ensure retries share the same ID
    private static final ConcurrentMap<String, String> testHistoryIds = new ConcurrentHashMap<>();
    
    // Store retry count per test
    private static final ConcurrentMap<String, Integer> testRetryCounts = new ConcurrentHashMap<>();
    
    /**
     * Process test result to ensure historyId is set correctly for retries
     * This should be called when a test result is created or updated
     */
    public static void processTestResult(TestResult testResult, String testKey) {
        if (testResult == null || testKey == null) {
            return;
        }
        
        // Get current retry count for this test
        Integer retryCount = testRetryCounts.get(testKey);
        if (retryCount == null) {
            retryCount = 0;
        }
        
        String currentHistoryId = testResult.getHistoryId();
        
        if (retryCount > 0) {
            // This is a retry - reuse the original historyId
            String originalHistoryId = testHistoryIds.get(testKey);
            if (originalHistoryId != null) {
                logger.debug("Retry {} reuse historyId {} for {}", retryCount, originalHistoryId, testKey);
                testResult.setHistoryId(originalHistoryId);
            } else {
                logger.warn("Retry {} without original historyId for {}", retryCount, testKey);
                // Store current historyId if we have one
                if (currentHistoryId != null && !currentHistoryId.isEmpty()) {
                    testHistoryIds.put(testKey, currentHistoryId);
                }
            }
        } else {
            // First attempt - store the historyId
            if (currentHistoryId != null && !currentHistoryId.isEmpty()) {
                testHistoryIds.put(testKey, currentHistoryId);
                logger.debug("Stored historyId {} for {}", currentHistoryId, testKey);
            } else {
                // Generate one if missing
                String generatedId = generateHistoryId(testResult);
                testResult.setHistoryId(generatedId);
                testHistoryIds.put(testKey, generatedId);
                logger.debug("Generated historyId {} for {}", generatedId, testKey);
            }
        }
    }
    
    /**
     * Update retry count for a test
     * Also syncs to common TestNG method key if it's a Cucumber scenario
     */
    public static void updateRetryCount(String testKey, int retryCount) {
        testRetryCounts.put(testKey, retryCount);
        logger.debug("Updated retry count {} for {}", retryCount, testKey);
        
        // If this is the TestNG method key, also update any Cucumber scenario keys that might be running
        // This helps sync retry counts between TestNG and Cucumber
        if (testKey.contains("TestRunner.runScenario")) {
            // This is the TestNG method - we'll sync it when we detect Cucumber scenarios
            logger.debug("TestNG method retry count updated");
        }
    }
    
    /**
     * Get retry count for a test
     */
    public static int getRetryCount(String testKey) {
        return testRetryCounts.getOrDefault(testKey, 0);
    }
    
    /**
     * Generate a consistent historyId for a test
     */
    private static String generateHistoryId(TestResult testResult) {
        String fullName = testResult.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            return String.valueOf(Math.abs(fullName.hashCode()));
        }
        String name = testResult.getName();
        if (name != null && !name.isEmpty()) {
            return String.valueOf(Math.abs(name.hashCode()));
        }
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Generate test key from test result
     * For Cucumber tests, this should match the scenario name
     */
    public static String generateTestKey(TestResult testResult) {
        // For Cucumber, fullName typically contains the scenario name
        // Format is usually: "Feature Name: Scenario Name" or just "Scenario Name"
        String fullName = testResult.getFullName();
        String name = testResult.getName();
        
        logger.debug("generateTestKey fullName={} name={}", fullName, name);
        
        // Use fullName if available, otherwise use name
        String key = fullName != null && !fullName.isEmpty() ? fullName : name;
        
        // For Cucumber, we might need to extract just the scenario name
        // But for now, use the full name as it should be consistent
        return key != null ? key : "unknown";
    }
    
    /**
     * Update test result via Allure lifecycle
     */
    public static void updateTestResultHistoryId(String uuid, String historyId) {
        AllureLifecycle lifecycle = io.qameta.allure.Allure.getLifecycle();
        if (uuid != null && historyId != null) {
            lifecycle.updateTestCase(uuid, testResult -> {
                testResult.setHistoryId(historyId);
                logger.debug("Updated test result {} with historyId {}", uuid, historyId);
            });
        }
    }
    
    /**
     * Get stored historyId for a test
     */
    public static String getHistoryId(String testKey) {
        return testHistoryIds.get(testKey);
    }
}
