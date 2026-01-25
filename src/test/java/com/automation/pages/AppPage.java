package com.automation.pages;

import com.automation.base.MobileBasePage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class AppPage extends MobileBasePage {
    private final By appMenu = AppiumBy.accessibilityId("App");
    private final By actionBar = AppiumBy.accessibilityId("Action Bar");
    private final By listItemText = By.id("android:id/text1");

    public void clickApp() {
        click(appMenu);
    }

    public boolean isActionBarDisplayed() {
        return isDisplayed(actionBar);
    }

    public List<String> getAppCategoryTexts() {
        List<WebElement> items = driver.findElements(listItemText);
        List<String> texts = new ArrayList<>();
        for (WebElement item : items) {
            String text = item.getText();
            if (text != null && !text.trim().isEmpty()) {
                texts.add(text.trim());
            }
        }
        return texts;
    }
}
