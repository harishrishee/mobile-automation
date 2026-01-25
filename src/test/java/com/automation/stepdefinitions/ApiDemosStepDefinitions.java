package com.automation.stepdefinitions;

import com.automation.base.MobileDriverManager;
import com.automation.pages.ApiDemosPage;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ApiDemosStepDefinitions {
    private AppiumDriver driver;
    private ApiDemosPage apiDemosPage;
    private List<String> lastCollectedTexts;

    @Given("the ApiDemos app is launched")
    public void the_api_demos_app_is_launched() {
        driver = MobileDriverManager.getDriver();
        Assert.assertNotNull(driver, "Mobile driver should be initialized");
        apiDemosPage = new ApiDemosPage();
        Assert.assertTrue(apiDemosPage.isHomeScreenVisible(), "ApiDemos home screen should be visible");
    }

    @Then("the home categories should be displayed")
    public void the_home_categories_should_be_displayed() {
        Assert.assertTrue(apiDemosPage.isHomeScreenVisible(), "Home categories should be displayed");
    }

    @Given("I am on the ApiDemos home screen")
    public void i_am_on_the_api_demos_home_screen() {
        apiDemosPage.relaunchApp();
        apiDemosPage.goToHomeScreen();
        Assert.assertTrue(apiDemosPage.isHomeScreenVisible(), "ApiDemos home screen should be visible");
    }

    @When("I open Alert Dialogs from App menu")
    public void i_open_alert_dialogs_from_app_menu() {
        apiDemosPage.openAlertDialogs();
    }

    @Then("the Alert Dialogs screen should be displayed")
    public void the_alert_dialogs_screen_should_be_displayed() {
        Assert.assertTrue(apiDemosPage.isAlertDialogsScreenVisible(), "Alert Dialogs screen should be visible");
    }

    @When("I open Controls in Light Theme")
    public void i_open_controls_in_light_theme() {
        apiDemosPage.openControlsLightTheme();
    }

    @When("I toggle the first checkbox")
    public void i_toggle_the_first_checkbox() {
        apiDemosPage.toggleFirstCheckbox();
    }

    @Then("the first checkbox should be checked")
    public void the_first_checkbox_should_be_checked() {
        Assert.assertTrue(apiDemosPage.isFirstCheckboxChecked(), "First checkbox should be checked");
    }

    @When("I open Preference dependencies")
    public void i_open_preference_dependencies() {
        apiDemosPage.openPreferenceDependencies();
    }

    @When("I enable WiFi and set WiFi settings to {string}")
    public void i_enable_wifi_and_set_wifi_settings_to(String wifiName) {
        apiDemosPage.enableWifiCheckbox();
        apiDemosPage.openWifiSettings();
        apiDemosPage.setWifiSettingsText(wifiName);
    }

    @Then("the WiFi settings should save {string}")
    public void the_wifi_settings_should_save(String wifiName) {
        apiDemosPage.openWifiSettings();
        String actual = apiDemosPage.readWifiSettingsText();
        Assert.assertEquals(actual, wifiName, "WiFi setting value should match");
    }

    @When("I open Date Widgets dialog")
    public void i_open_date_widgets_dialog() {
        apiDemosPage.openDateWidgetsDialog();
    }

    @When("I open the date picker")
    public void i_open_the_date_picker() {
        apiDemosPage.openDatePicker();
    }

    @Then("the date picker should be displayed")
    public void the_date_picker_should_be_displayed() {
        Assert.assertTrue(apiDemosPage.isDatePickerVisible(), "Date picker should be visible");
    }

    @When("I collect list item texts")
    public void i_collect_list_item_texts() {
        lastCollectedTexts = apiDemosPage.getTextsByResourceId("android:id/text1");
        Assert.assertFalse(lastCollectedTexts.isEmpty(), "List items should be found");
    }

    @Then("the list item texts should be {string}")
    public void the_list_item_texts_should_be(String expectedCsv) {
        List<String> expected = Arrays.stream(expectedCsv.split(","))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
        Assert.assertEquals(lastCollectedTexts, expected, "List item texts should match");
    }
}
