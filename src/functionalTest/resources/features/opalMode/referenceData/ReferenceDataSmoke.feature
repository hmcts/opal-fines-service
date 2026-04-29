@Opal @JIRA-LABEL:reference-data
Feature: Reference Data Smoke Checks

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-313 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6238
  Scenario: Business units reference data is available
    When I make a request to the business unit ref data api filtering by business unit type "Area"
    Then the business unit ref data matching to result

  @JIRA-STORY:PO-311 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6240
  Scenario: Offence reference data is available
    When I make a request to the offence ref data api filtering by cjs code "AA06"
    Then the offence ref data matching to result


  @JIRA-STORY:PO-349 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6242
  Scenario: Major creditor reference data is available
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the major creditors ref data matching to result

  @JIRA-STORY:PO-312 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6243
  Scenario: Local justice area reference data is available
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @JIRA-STORY:PO-316 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6245
  Scenario: Enforcer reference data is available
    When I make a request to enforcer ref data api filtering by name "Alder"
    Then the enforcer ref data matching to result
