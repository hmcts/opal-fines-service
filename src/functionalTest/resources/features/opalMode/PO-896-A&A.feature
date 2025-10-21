Feature: Authentication & authorisation parity after shared-lib move

  Scenario: Expired token returns 401
    Given I am testing with an expired token for the "opal-test-10@hmcts.net" user
    When I make a request to the business unit ref data api filtering by business unit type "area"
    Then the response status is 401

