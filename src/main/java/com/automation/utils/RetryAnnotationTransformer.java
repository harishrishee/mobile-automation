package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryAnnotationTransformer implements IAnnotationTransformer {
    private static final Logger logger = LogManager.getLogger(RetryAnnotationTransformer.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        boolean attached = attachRetryAnalyzer(annotation);
        if (attached) {
            if (testMethod != null) {
                logger.info("RetryAnalyzer attached to test method: {}", testMethod.getName());
            } else if (testClass != null) {
                logger.info("RetryAnalyzer attached to test class: {}", testClass.getName());
            } else {
                logger.info("RetryAnalyzer attached via annotation transformer");
            }
        }
    }

    private boolean attachRetryAnalyzer(ITestAnnotation annotation) {
        try {
            Method getRetryAnalyzerClass = annotation.getClass().getMethod("getRetryAnalyzerClass");
            Object existing = getRetryAnalyzerClass.invoke(annotation);
            if (existing != null) {
                return false;
            }
            Method setRetryAnalyzerClass = annotation.getClass()
                    .getMethod("setRetryAnalyzerClass", Class.class);
            setRetryAnalyzerClass.invoke(annotation, RetryAnalyzer.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            // Fall back to older TestNG method names
        } catch (Exception e) {
            logger.warn("Failed to set retry analyzer using get/setRetryAnalyzerClass: {}", e.getMessage());
        }

        try {
            Method getRetryAnalyzer = annotation.getClass().getMethod("getRetryAnalyzer");
            Object existing = getRetryAnalyzer.invoke(annotation);
            if (existing != null) {
                return false;
            }
            Method setRetryAnalyzer = annotation.getClass()
                    .getMethod("setRetryAnalyzer", Class.class);
            setRetryAnalyzer.invoke(annotation, RetryAnalyzer.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            logger.warn("Retry analyzer methods not available on ITestAnnotation implementation");
        } catch (Exception e) {
            logger.warn("Failed to set retry analyzer using get/setRetryAnalyzer: {}", e.getMessage());
        }

        return false;
    }
}
