package com.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;
import org.testng.annotations.DataProvider;
import com.automation.listeners.AllureRetryListener;
import com.automation.utils.ConfigReader;
import com.automation.utils.RetryAnnotationTransformer;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.automation.stepdefinitions", "com.automation.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/html",
                "json:target/cucumber-reports/Cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
@Listeners({AllureRetryListener.class, RetryAnnotationTransformer.class})
public class TestRunner extends AbstractTestNGCucumberTests {
    static {
        String cliTags = System.getProperty("tags");
        if (cliTags == null || cliTags.trim().isEmpty()) {
            cliTags = System.getProperty("cucumber.filter.tags");
        }

        if (cliTags != null && !cliTags.trim().isEmpty()) {
            System.setProperty("cucumber.filter.tags", cliTags.trim());
        } else {
            String configTags = ConfigReader.getProperty("cucumber.tags", "").trim();
            if (!configTags.isEmpty()) {
                System.setProperty("cucumber.filter.tags", configTags);
            }
        }
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
