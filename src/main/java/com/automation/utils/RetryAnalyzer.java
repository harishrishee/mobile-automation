package com.automation.utils;

import com.automation.listeners.AllureRetryExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private static final Map<String, Integer> RETRY_COUNTS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> CURRENT_RETRY = new ThreadLocal<>();

    private final int maxRetryCount;

    public RetryAnalyzer() {
        this.maxRetryCount = resolveRetryCount();
        logger.info("RetryAnalyzer initialized with maxRetryCount={}", maxRetryCount);
    }

    @Override
    public boolean retry(ITestResult result) {
        if (maxRetryCount <= 0) {
            return false;
        }

        String testKey = getTestKey(result);
        int currentRetry = RETRY_COUNTS.getOrDefault(testKey, 0);

        if (currentRetry < maxRetryCount) {
            int nextRetry = currentRetry + 1;
            RETRY_COUNTS.put(testKey, nextRetry);
            CURRENT_RETRY.set(nextRetry);
            AllureRetryExtension.updateRetryCount(testKey, nextRetry);

            logger.info("Retrying test: {} (attempt {}/{})",
                    result.getMethod().getMethodName(), nextRetry, maxRetryCount);
            return true;
        }

        logger.info("Retry limit reached for test: {}", testKey);
        return false;
    }

    public static int getRetryCountForTest(ITestResult result) {
        if (result == null) {
            return 0;
        }
        return RETRY_COUNTS.getOrDefault(getTestKey(result), 0);
    }

    public static int getRetryCountForTestKey(String testKey) {
        if (testKey == null) {
            return 0;
        }
        return RETRY_COUNTS.getOrDefault(testKey, 0);
    }

    public static int getCurrentThreadRetryCount() {
        Integer retry = CURRENT_RETRY.get();
        return retry != null ? retry : 0;
    }

    private static String getTestKey(ITestResult result) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        String paramsStr = params != null ? Arrays.toString(params) : "";
        return className + "." + methodName + "(" + paramsStr + ")";
    }

    private int resolveRetryCount() {
        String envRetry = System.getProperty("retry.count");
        if (envRetry != null && !envRetry.trim().isEmpty()) {
            return parseRetryCount(envRetry, 0);
        }
        String configRetry = ConfigReader.getProperty("retry.count", "0");
        return parseRetryCount(configRetry, 0);
    }

    private int parseRetryCount(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid retry count value '{}', using {}", value, defaultValue);
            return defaultValue;
        }
    }
}
