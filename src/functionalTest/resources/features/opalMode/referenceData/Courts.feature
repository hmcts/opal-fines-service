@Opal @JIRA-LABEL:reference-data
Feature: Courts Reference Data

  @JIRA-STORY:PO-315 @JIRA-EPIC:PO-304
  #PO-315

  Scenario: Checking the court reference data endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a filter of "Yeovil FPC"
    Then the court ref data matching to result
