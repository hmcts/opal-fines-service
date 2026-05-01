@Opal @JIRA-LABEL:reference-data
Feature: Offences Reference Data

  @JIRA-STORY:PO-311 @JIRA-EPIC:PO-304

  Scenario: Offences can be retrieved by CJS code
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by cjs code "AA06"
    Then the offence ref data matching to result
