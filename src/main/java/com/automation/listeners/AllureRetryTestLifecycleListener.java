package com.automation.listeners;

import com.automation.utils.RetryAnalyzer;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Allure Test Lifecycle Listener that intercepts test result creation
 * This uses Allure's SPI mechanism to hook into the lifecycle
 */
public class AllureRetryTestLifecycleListener implements TestLifecycleListener {
    private static final Logger logger = LogManager.getLogger(AllureRetryTestLifecycleListener.class);
    
    @Override
    public void beforeTestStart(TestResult result) {
        if (result == null) {
            return;
        }
        
        String testKey = AllureRetryExtension.generateTestKey(result);
        int retryCount = getRetryCountWithSync(testKey);
        
        logger.debug("beforeTestStart key={} name={} retry={} uuid={}",
                testKey, result.getName(), retryCount, result.getUuid());
        
        // Process the test result to ensure historyId is set correctly
        AllureRetryExtension.processTestResult(result, testKey);
        
        if (retryCount > 0) {
            String originalHistoryId = AllureRetryExtension.getHistoryId(testKey);
            if (originalHistoryId != null) {
                result.setHistoryId(originalHistoryId);
                logger.debug("beforeTestStart set historyId {} retry={}", originalHistoryId, retryCount);
            } else {
                logger.warn("Retry {} without historyId for {}", retryCount, testKey);
            }
            
            // CRITICAL: Generate a new UUID for retries so Allure creates a separate result file
            // This is the key to making retries appear in the Retries tab
            String originalUuid = result.getUuid();
            String newUuid = java.util.UUID.randomUUID().toString();
            
            // Update the UUID in the result
            result.setUuid(newUuid);
            
            // Also update it in Allure lifecycle if the test has already started
            try {
                AllureLifecycle lifecycle = Allure.getLifecycle();
                String currentUuid = lifecycle.getCurrentTestCase().orElse(null);
                if (currentUuid != null && currentUuid.equals(originalUuid)) {
                    // The test has started, we need to stop it and start a new one
                    // But this is complex with Cucumber, so we'll just update the UUID
                    logger.debug("Test already started, updating UUID {} -> {} (retry {})",
                            originalUuid, newUuid, retryCount);
                } else {
                    logger.debug("Generated new UUID {} (retry {}, original {})", newUuid, retryCount, originalUuid);
                }
            } catch (Exception e) {
                logger.warn("Could not update Allure lifecycle for new UUID: {}", e.getMessage());
            }
            
            // Add retry label and parameter to make retries visible
            List<Label> labels = result.getLabels();
            if (labels == null) {
                labels = new ArrayList<>();
                result.setLabels(labels);
            }
            // Add retry label
            labels.add(new Label().setName("retry").setValue("true"));
            labels.add(new Label().setName("retry_attempt").setValue(String.valueOf(retryCount)));
            
            // Add retry parameter
            Allure.label("retry", "true");
            Allure.label("retry_attempt", String.valueOf(retryCount));
            Allure.parameter("Retry Attempt", String.valueOf(retryCount));
        } else {
            // First attempt - log the historyId we're storing
            String historyId = result.getHistoryId();
            logger.debug("First attempt historyId={} key={} uuid={}", historyId, testKey, result.getUuid());
        }
    }
    
    /**
     * Get retry count, checking both Cucumber key and TestNG method key
     * Also checks RetryAnalyzer directly to get the most up-to-date retry count
     */
    private int getRetryCountWithSync(String testKey) {
        int retryCount = AllureRetryExtension.getRetryCount(testKey);
        
        // If not found, check TestNG method key and sync
        if (retryCount == 0) {
            String testngKey = "com.automation.runners.TestRunner.runScenario";
            
            // First check AllureRetryExtension
            retryCount = AllureRetryExtension.getRetryCount(testngKey);
            
            // If still 0, try to get it directly from RetryAnalyzer
            if (retryCount == 0) {
                retryCount = RetryAnalyzer.getRetryCountForTestKey(testngKey);
                if (retryCount > 0) {
                    logger.debug("Retry count {} from RetryAnalyzer for {}", retryCount, testngKey);
                }
            }
            
            if (retryCount > 0) {
                AllureRetryExtension.updateRetryCount(testKey, retryCount);
                AllureRetryExtension.updateRetryCount(testngKey, retryCount); // Ensure it's also in extension
                logger.debug("Synced retry count {} to {}", retryCount, testKey);
            }
        }
        
        return retryCount;
    }
    
    @Override
    public void afterTestStart(TestResult result) {
        if (result == null) {
            return;
        }
        
        String testKey = AllureRetryExtension.generateTestKey(result);
        int retryCount = getRetryCountWithSync(testKey);
        
        if (retryCount > 0) {
            String originalHistoryId = AllureRetryExtension.getHistoryId(testKey);
            if (originalHistoryId != null && !originalHistoryId.equals(result.getHistoryId())) {
                result.setHistoryId(originalHistoryId);
                logger.debug("afterTestStart set historyId {} retry={}", originalHistoryId, retryCount);
            }
        } else {
            // First attempt - ensure we store the historyId
            String historyId = result.getHistoryId();
            if (historyId != null && !historyId.isEmpty()) {
                AllureRetryExtension.processTestResult(result, testKey);
                logger.debug("afterTestStart stored historyId {}", historyId);
            }
        }
    }
    
    @Override
    public void beforeTestUpdate(TestResult result) {
        if (result == null) {
            return;
        }
        
        String testKey = AllureRetryExtension.generateTestKey(result);
        int retryCount = getRetryCountWithSync(testKey);
        
        if (retryCount > 0) {
            String originalHistoryId = AllureRetryExtension.getHistoryId(testKey);
            if (originalHistoryId != null && !originalHistoryId.equals(result.getHistoryId())) {
                result.setHistoryId(originalHistoryId);
                logger.debug("beforeTestUpdate set historyId {} retry={}", originalHistoryId, retryCount);
            }
        }
    }
    
    @Override
    public void afterTestUpdate(TestResult result) {
        if (result == null) {
            return;
        }
        
        String testKey = AllureRetryExtension.generateTestKey(result);
        int retryCount = getRetryCountWithSync(testKey);
        
        if (retryCount > 0) {
            String originalHistoryId = AllureRetryExtension.getHistoryId(testKey);
            if (originalHistoryId != null && !originalHistoryId.equals(result.getHistoryId())) {
                result.setHistoryId(originalHistoryId);
                logger.debug("afterTestUpdate set historyId {} retry={}", originalHistoryId, retryCount);
            }
        }
    }
}

