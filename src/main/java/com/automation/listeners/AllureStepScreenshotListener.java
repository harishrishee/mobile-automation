package com.automation.listeners;

import com.automation.base.MobileDriverManager;
import com.automation.utils.ConfigReader;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StepResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * Allure Step Lifecycle Listener that intercepts step completion
 * and attaches screenshots to Cucumber steps automatically
 * This uses Allure's SPI mechanism to hook into the step lifecycle
 */
public class AllureStepScreenshotListener implements StepLifecycleListener {
    private static final Logger logger = LogManager.getLogger(AllureStepScreenshotListener.class);
    
    @Override
    public void beforeStepStart(StepResult result) {
        // Step is starting - we can capture step UUID here if needed
    }
    
    @Override
    public void afterStepUpdate(StepResult result) {
        // Step has been updated - this is called during step execution
    }
    
    // Track which steps already have screenshots to prevent duplicates
    private static final java.util.Set<String> stepsWithScreenshots = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    
    @Override
    public void beforeStepStop(StepResult result) {
        // Step is about to stop - attach screenshot inline
        if (result == null) {
            return;
        }
        
        // Only attach screenshots if configured
        if (!isScreenshotForSteps()) {
            return;
        }
        
        // Skip hook steps and API test steps
        String stepName = result.getName();
        if (stepName == null || 
            stepName.contains("Hooks.") || 
            stepName.contains("com.automation.hooks") ||
            stepName.contains("afterStep") ||
            stepName.contains("Tear down") ||
            stepName.contains("Set up") ||
            stepName.contains("API test") ||
            stepName.contains("I make a GET request") ||
            stepName.contains("I make a POST request") ||
            stepName.contains("I make a PUT request") ||
            stepName.contains("I make a DELETE request") ||
            stepName.contains("I make a PATCH request") ||
            stepName.contains("response status code") ||
            stepName.contains("response should contain")) {
            return;
        }
        
        try {
            AllureLifecycle lifecycle = Allure.getLifecycle();
            String stepUuid = lifecycle.getCurrentTestCaseOrStep().orElse(null);
            if (stepUuid == null) {
                return;
            }
            
            String testCaseUuid = lifecycle.getCurrentTestCase().orElse(null);
            if (testCaseUuid == null || stepUuid.equals(testCaseUuid)) {
                // This is the test case, not a step - skip it
                return;
            }
            
            // Prevent duplicate attachments to the same step
            String stepKey = testCaseUuid + "_" + stepName;
            if (stepsWithScreenshots.contains(stepKey)) {
                return;
            }
            
            WebDriver driver = MobileDriverManager.getDriver();
            // Skip screenshot if driver is null (API tests don't have a driver)
            if (driver == null || !(driver instanceof TakesScreenshot)) {
                return;
            }
            
            // Capture screenshot as bytes (no PNG file created)
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (screenshotBytes == null || screenshotBytes.length == 0) {
                return;
            }
            
            // Simple attachment name
            String attachmentName = UUID.randomUUID().toString();
            
            // Attach screenshot inline to the step
            lifecycle.updateStep(stepUuid, step -> {
                // Check if screenshot already attached
                boolean hasScreenshot = step.getAttachments() != null &&
                    step.getAttachments().stream()
                        .anyMatch(att -> att.getName() != null && att.getName().equals(attachmentName));
                
                if (!hasScreenshot) {
                    String attachmentSource = lifecycle.prepareAttachment(attachmentName, "image/png", "png");
                    if (attachmentSource != null) {
                        lifecycle.writeAttachment(attachmentSource, new ByteArrayInputStream(screenshotBytes));
                        stepsWithScreenshots.add(stepKey);
                    }
                }
            });
        } catch (Exception e) {
            logger.debug("Could not attach screenshot: {}", e.getMessage());
        }
    }
    
    @Override
    public void afterStepStop(StepResult result) {
        // Step has stopped - too late to attach here, step is finalized
    }

    private boolean isScreenshotForSteps() {
        return Boolean.parseBoolean(ConfigReader.getProperty("screenshot.steps", "false"));
    }
}

