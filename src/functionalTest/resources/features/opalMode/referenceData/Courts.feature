@Opal @JIRA-LABEL:reference-data
Feature: Courts Reference Data

  @JIRA-STORY:PO-315 @JIRA-EPIC:PO-304

  @JIRA-TEST-KEY:PO-5706
  Scenario: Courts can be retrieved by name
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a filter of "Yeovil FPC"
    Then the court ref data matching to result
