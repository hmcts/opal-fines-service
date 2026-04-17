@Opal @JIRA-LABEL:reference-data
Feature: Major Creditors Reference Data

  @JIRA-STORY:PO-349 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6202
  Scenario: Verifying the major creditors reference data endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the major creditors ref data matching to result
