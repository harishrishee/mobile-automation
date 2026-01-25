package com.automation.pages;

import com.automation.base.MobileBasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;
import java.util.Map;
public class MyDemoAppPage extends MobileBasePage {

    private static final Logger logger = LogManager.getLogger(MyDemoAppPage.class);

    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeStaticText[`name == \"Login\"`][1]")
    private WebElement loginTitle;

    @iOSXCUITFindBy(accessibility = "Login Button")
    private WebElement loginButton;

    @iOSXCUITFindBy(accessibility = "Catalog-tab-item")
    private WebElement catalogTab;

    @iOSXCUITFindBy(accessibility = "title")
    private WebElement productsTitle;

    @iOSXCUITFindBy(accessibility = "More-tab-item")
    private WebElement moreButton;

    @iOSXCUITFindBy(className = "XCUIElementTypeTextField")
    private WebElement userNameField;

    @iOSXCUITFindBy(className = "XCUIElementTypeSecureTextField")
    private WebElement passwordField;

    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeButton[`name == \"Login\"`]")
    private WebElement loginButton1;


    public MyDemoAppPage() {
        super();
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    private By productsTitle(String productName) {
        return By.xpath("//XCUIElementTypeStaticText[@name='Product Name' and @label='" + productName + "']");
    }


    private By usernameButton(String username) {
        return AppiumBy.accessibilityId(username);
    }

    public boolean isProductsPageDisplayed() {
        return isDisplayedElement(productsTitle);
    }

    public boolean isLoginButtonVisible() {
        return isDisplayedElement(loginButton);
    }


    public void selectUsername(String username) {
        userNameField.clear();
        userNameField.sendKeys(username);
    }

    public void enterPassword(String password) {
        passwordField.clear();
        passwordField.sendKeys(password);
        hideKeyboardIfVisible();
    }

    public void tapLogin() {
        loginButton1.click();
    }

    public boolean isCatalogTabVisible() {
        return isDisplayedElement(catalogTab);
    }

    public void iClickOnTheMoreButton() {
        moreButton.click();
    }

    public void iClickOnTheLoginButton() {
        loginButton.click();
    }

    public boolean isLoginTitleDisplayed() {
       return isDisplayedElement(loginTitle);
    }

    private void hideKeyboardIfVisible() {
        try {
            if (driver instanceof IOSDriver) {
                try {
                    driver.executeScript("mobile: hideKeyboard", Map.of());
                    return;
                } catch (Exception ignored) {
                    // fall through to alternate dismiss strategies
                }
                dismissKeyboardWithReturn();
            }
        } catch (Exception ignored) {
            logger.warn("Failed to hide keyboard: {}", ignored.getMessage());
        }
    }

    private void dismissKeyboardWithReturn() {
        try {
            WebElement returnKey = driver.findElement(AppiumBy.accessibilityId("return"));
            returnKey.click();
            return;
        } catch (Exception ignored) {
            // try "Done"
        }
        try {
            WebElement doneKey = driver.findElement(AppiumBy.accessibilityId("Done"));
            doneKey.click();
            return;
        } catch (Exception ignored) {
            // tap outside as last resort
        }
        try {
            driver.executeScript("mobile: tap", Map.of("x", 10, "y", 10));
        } catch (Exception ignored) {
            // no-op
        }
    }

    public void iClickOnTheProductButton(String productName) {

        Assert.assertEquals(driver.findElement(productsTitle(productName)).getAttribute("type"), "XCUIElementTypeStaticText");
        click(productsTitle(productName));

    }

    private boolean isDisplayedElement(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
