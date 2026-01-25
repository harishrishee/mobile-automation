package com.automation.base;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;

public class MobileBasePage {
    private static final Logger logger = LogManager.getLogger(MobileBasePage.class);
    protected AppiumDriver driver;
    protected WebDriverWait wait;

    public MobileBasePage() {
        this.driver = MobileDriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement findElement(By locator) {
        logger.debug("findElement: {}", locator);
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void click(By locator) {
        logger.debug("click: {}", locator);
        findElement(locator).click();
    }

    protected void sendKeys(By locator, String text) {
        logger.debug("sendKeys: {}", locator);
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        logger.debug("getText: {}", locator);
        return findElement(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        logger.debug("isDisplayed: {}", locator);
        return findElement(locator).isDisplayed();
    }

    private void performSwipe(int startX, int startY, int endX, int endY) {
        logger.debug("swipe: ({},{}) -> ({},{})", startX, startY, endX, endY);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }

    protected void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        int x = size.width / 2;
        performSwipe(x, startY, x, endY);
    }

    protected void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        int x = size.width / 2;
        performSwipe(x, startY, x, endY);
    }

    protected void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        int y = size.height / 2;
        performSwipe(startX, y, endX, y);
    }

    protected void swipeRight() {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        int y = size.height / 2;
        performSwipe(startX, y, endX, y);
    }
}
