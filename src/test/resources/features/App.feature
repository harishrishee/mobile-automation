@ApiDemos
Feature: ApiDemos App Tests
  Test ApiDemos app functionality on emulator

  Background:
    Given the ApiDemos app is launched
    And I am on the ApiDemos home screen

  Scenario: Verify App screen categories
    When I open App from the home screen
    Then the App categories should be displayed
    And the App category list should be "Action Bar, Activity, Alarm, Alert Dialogs, Device Admin, Fragment, Launcher Shortcuts, Loader, Menu, Notification, Search, Service, Text-To-Speech, Voice Recognition"

