Feature: User Signup

  Scenario: User signs up with valid data
    Given user launches Demoblaze application
    When user signs up with valid data
    Then signup should be successful