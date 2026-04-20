@Opal @JIRA-LABEL:reference-data
Feature: Business Units Reference Data


  @JIRA-STORY:PO-313 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6183
  Scenario: Verifying the business units reference data endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the business unit ref data api filtering by business unit type "Area"
    Then the business unit ref data matching to result
