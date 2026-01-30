Feature: Cart functionality

  Background:
    Given user launches Demoblaze application
    And user logs in with valid data

  Scenario: Add product to cart
    When user selects product "Samsung galaxy s6"
    And user adds product to cart
    Then product should be added to cart successfully

  Scenario: Verify product in cart
    When user navigates to cart page
    Then selected product should be displayed in cart

  Scenario: Remove product from cart
    When user navigates to cart page
    And user deletes the product from cart
    Then cart should be empty

  Scenario: Place order successfully
    When user selects product "Samsung galaxy s6"
    And user adds product to cart
    And user navigates to cart page
    And user clicks on Place Order
    And user enters order details
    And user confirms the purchase
    Then order should be placed successfully

