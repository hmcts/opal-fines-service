@Opal @JIRA-LABEL:reference-data @R1CReferenceData
Feature: Major Creditors Reference Data

  @JIRA-STORY:PO-349 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5715
  Scenario: A major creditor can be retrieved by identifier
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the major creditors ref data matching to result

  @JIRA-STORY:PO-2972 @JIRA-EPIC:PO-2630 @R1B
  Scenario: Major creditor reference data exposes repayment
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request the major creditor reference data
    Then the response contains repayment and does not contain from suspense
