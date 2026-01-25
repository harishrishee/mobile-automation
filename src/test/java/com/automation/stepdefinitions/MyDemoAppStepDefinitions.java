package com.automation.stepdefinitions;

import com.automation.base.MobileDriverManager;
import com.automation.pages.MyDemoAppPage;
import io.appium.java_client.AppiumDriver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class MyDemoAppStepDefinitions {
    private AppiumDriver driver;
    private MyDemoAppPage myDemoAppPage;

    @Given("the iOS MyDemoApp is launched")
    public void the_ios_my_demo_app_is_launched() {
        driver = MobileDriverManager.getDriver();
        Assert.assertNotNull(driver, "Driver should be initialized");
        myDemoAppPage = new MyDemoAppPage();
       // Assert.assertTrue(myDemoAppPage.isProductsPageDisplayed(), "Login screen should be visible");
    }

    @Then("the login screen should be displayed")
    public void the_login_screen_should_be_displayed() {
        Assert.assertTrue(myDemoAppPage.isLoginTitleDisplayed(), "Login button should be visible");
    }

    @When("I select username {string}")
    public void i_select_username(String username) {
        myDemoAppPage.selectUsername(username);
    }

    @When("I enter password {string}")
    public void i_enter_password(String password) {
        myDemoAppPage.enterPassword(password);
    }

    @When("I tap Login")
    public void i_tap_login() {
        myDemoAppPage.tapLogin();
    }

    @Then("the catalog tab should be visible")
    public void the_catalog_tab_should_be_visible() {
        Assert.assertTrue(myDemoAppPage.isCatalogTabVisible(), "Catalog tab should be visible");
    }

    @When("i click on the more button")
    public void iClickOnTheMoreButton() {
        myDemoAppPage.iClickOnTheMoreButton();
    }

    @When("i click on the login button")
    public void iClickOnTheLoginButton() {
        myDemoAppPage.iClickOnTheLoginButton();
    }

    @When("i click on the product button {string}")
    public void iClickOnTheProductButton(String productName) {
        myDemoAppPage.iClickOnTheProductButton(productName);
    }
}
