@Opal @JIRA-LABEL:reference-data
Feature: Offences Reference Data

  @JIRA-STORY:PO-311 @JIRA-EPIC:PO-304
  #PO-311
  Scenario: Checking the offence reference data endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by cjs code "AA06"
    Then the offence ref data matching to result
