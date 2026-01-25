package com.automation.pages;

import com.automation.base.MobileBasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ApiDemosPage extends MobileBasePage {
    // Home screen categories
    private final By accessibilityMenu = AppiumBy.accessibilityId("Accessibility");
    private final By appMenu = AppiumBy.accessibilityId("App");
    private final By viewsMenu = AppiumBy.accessibilityId("Views");
    private final By preferenceMenu = AppiumBy.accessibilityId("Preference");

    // App -> Alert Dialogs
    private final By alertDialogsMenu = AppiumBy.accessibilityId("Alert Dialogs");
    private final By okCancelMessageItem = AppiumBy.accessibilityId("OK Cancel dialog with a message");

    // Views -> Controls -> 1. Light Theme
    private final By controlsMenu = AppiumBy.accessibilityId("Controls");
    private final By lightThemeMenu = AppiumBy.accessibilityId("1. Light Theme");
    private final By checkbox1 = By.id("io.appium.android.apis:id/check1");

    // Preference -> 3. Preference dependencies
    private final By preferenceDependenciesMenu = AppiumBy.accessibilityId("3. Preference dependencies");
    private final By wifiCheckbox = By.id("android:id/checkbox");
    private final By wifiSettingsInput = By.id("android:id/edit");
    private final By dialogOkButton = By.id("android:id/button1");

    // Views -> Date Widgets -> 1. Dialog
    private final By dateWidgetsMenu = AppiumBy.accessibilityId("Date Widgets");
    private final By dateWidgetsDialogMenu = AppiumBy.accessibilityId("1. Dialog");
    private final By changeDateButton = By.id("io.appium.android.apis:id/changeDate");
    private final By datePicker = By.id("android:id/datePicker");

    private By byText(String text) {
        return AppiumBy.androidUIAutomator("new UiSelector().text(\"" + text + "\")");
    }

    public boolean isHomeScreenVisible() {
        return isDisplayed(accessibilityMenu) && isDisplayed(appMenu);
    }

    public void goToHomeScreen() {
        int maxTries = 5;
        for (int i = 0; i < maxTries; i++) {
            if (isHomeScreenVisible()) {
                return;
            }
            driver.navigate().back();
        }
        relaunchApp();
        if (!isHomeScreenVisible()) {
            throw new RuntimeException("Unable to return to ApiDemos home screen after relaunch");
        }
    }

    public void relaunchApp() {
        String appPackage = null;
        Object pkgCapability = driver.getCapabilities().getCapability("appPackage");
        if (pkgCapability != null) {
            appPackage = pkgCapability.toString();
        }
        if (appPackage == null || appPackage.isEmpty()) {
            appPackage = "io.appium.android.apis";
        }
        try {
            if (driver instanceof io.appium.java_client.android.AndroidDriver) {
                io.appium.java_client.android.AndroidDriver androidDriver =
                        (io.appium.java_client.android.AndroidDriver) driver;
                forceStopApp(androidDriver, appPackage);
                Thread.sleep(1000);
                androidDriver.activateApp(appPackage);
                waitForAppToBeForeground(androidDriver, appPackage, 10);
            } else {
                throw new RuntimeException("Relaunch is only supported for Android in this project");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to relaunch ApiDemos app", e);
        }
    }

    private void waitForAppToBeForeground(
            io.appium.java_client.android.AndroidDriver androidDriver,
            String appPackage,
            int maxSeconds
    ) throws InterruptedException {
        for (int i = 0; i < maxSeconds; i++) {
            String currentPackage = androidDriver.getCurrentPackage();
            if (appPackage.equals(currentPackage)) {
                return;
            }
            Thread.sleep(1000);
        }
    }

    private void forceStopApp(
            io.appium.java_client.android.AndroidDriver androidDriver,
            String appPackage
    ) {
        try {
            androidDriver.terminateApp(appPackage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to force-stop app", e);
        }
    }

    public void openAlertDialogs() {
        click(appMenu);
        click(alertDialogsMenu);
    }

    public boolean isAlertDialogsScreenVisible() {
        return isDisplayed(okCancelMessageItem);
    }

    public void openControlsLightTheme() {
        click(viewsMenu);
        click(controlsMenu);
        click(lightThemeMenu);
    }

    public void toggleFirstCheckbox() {
        click(checkbox1);
    }

    public boolean isFirstCheckboxChecked() {
        WebElement checkbox = findElement(checkbox1);
        return Boolean.parseBoolean(checkbox.getAttribute("checked"));
    }

    public void openPreferenceDependencies() {
        click(preferenceMenu);
        click(preferenceDependenciesMenu);
    }

    public void enableWifiCheckbox() {
        if (!Boolean.parseBoolean(findElement(wifiCheckbox).getAttribute("checked"))) {
            click(wifiCheckbox);
        }
    }

    public void openWifiSettings() {
        click(byText("WiFi settings"));
    }

    public void setWifiSettingsText(String text) {
        sendKeys(wifiSettingsInput, text);
        click(dialogOkButton);
    }

    public String readWifiSettingsText() {
        String value = getText(wifiSettingsInput);
        click(dialogOkButton);
        return value;
    }

    public void openDateWidgetsDialog() {
        click(viewsMenu);
        click(dateWidgetsMenu);
        click(dateWidgetsDialogMenu);
    }

    public void openDatePicker() {
        click(changeDateButton);
    }

    public boolean isDatePickerVisible() {
        return isDisplayed(datePicker);
    }

    public List<String> getTextsByResourceId(String resourceId) {
        List<WebElement> elements = driver.findElements(By.id(resourceId));
        List<String> texts = new ArrayList<>();
        for (WebElement element : elements) {
            String text = element.getText();
            if (text != null && !text.trim().isEmpty()) {
                texts.add(text.trim());
            }
        }
        return texts;
    }
}
