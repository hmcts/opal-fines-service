@Opal @PO-313
Feature: Verifying the end points for business units


  @JIRA-KEY:POT-156
  Scenario: verifying the end points for API for Business Units Ref Data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the business unit ref data api filtering by business unit type "area"
    Then the business unit ref data matching to result

