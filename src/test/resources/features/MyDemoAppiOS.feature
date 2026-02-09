@iOS
@MyDemoApp
Feature: iOS MyDemoApp Tests
  Validate MyDemoApp login flow on iOS simulator

  Background:
    Given the iOS MyDemoApp is launched

  Scenario: Verify login screen
    When i click on the more button
    And i click on the login button
    Then the login screen should be displayed

  Scenario: Login with a listed user
    When i click on the more button
    And i click on the login button
    Then the login screen should be displayed
    When I select username "bob@example.com"
    And I enter password "10203040"
    And I tap Login
    Then the catalog tab should be visible

