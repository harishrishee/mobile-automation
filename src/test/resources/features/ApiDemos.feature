@ApiDemos
Feature: ApiDemos App Tests
  Test ApiDemos app functionality on emulator

  Background:
    Given the ApiDemos app is launched
    And I am on the ApiDemos home screen

  Scenario: Verify ApiDemos home screen categories
    Then the home categories should be displayed

  Scenario: Navigate to Alert Dialogs
    When I open Alert Dialogs from App menu
    Then the Alert Dialogs screen should be displayed

  Scenario: Toggle a checkbox in Controls
    When I open Controls in Light Theme
    And I toggle the first checkbox
    Then the first checkbox should be checked

  Scenario: Set WiFi preference dependency
    When I open Preference dependencies
    And I enable WiFi and set WiFi settings to "TestWifi"
    Then the WiFi settings should save "TestWifi"

  Scenario: Open date picker dialog
    When I open Date Widgets dialog
    And I open the date picker
    Then the date picker should be displayed
