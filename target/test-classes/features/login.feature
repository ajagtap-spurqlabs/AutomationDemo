@login
Feature: User Login

  Background:
    Given user launches Demoblaze application

  Scenario: User logs in successfully
    When user logs in with valid data
    Then login should be successful
