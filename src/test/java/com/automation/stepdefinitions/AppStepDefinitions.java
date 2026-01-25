package com.automation.stepdefinitions;

import com.automation.pages.AppPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppStepDefinitions {

    private final AppPage appPage = new AppPage();
    private List<String> appCategoryTexts;

    @Given("I open App from the home screen")
    public void i_open_App_from_the_home_screen() {
        appPage.clickApp();
    }

    @Then("the App categories should be displayed")
    public void theAppCategoriesShouldBeDisplayed() {
        Assert.assertTrue(appPage.isActionBarDisplayed(), "App categories should be displayed");
    }

    @Then("the App category list should be {string}")
    public void theAppCategoryListShouldBe(String expectedCsv) {
        appCategoryTexts = appPage.getAppCategoryTexts();
        List<String> expected = Arrays.stream(expectedCsv.split(","))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
        Assert.assertEquals(appCategoryTexts, expected, "App category list should match");
    }
}
