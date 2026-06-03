@Opal @JIRA-LABEL:reference-data
Feature: Major Creditors Business Unit Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-421 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5716
  Scenario: Major creditor responses include business-unit data
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the response contains the below major creditor data
      | major_creditor_id   | 1300000000075               |
      | name                | LORD CHANCELLORS DEPARTMENT |
      | major_creditor_code | 0096                        |
      | business_unit_id    | 130                         |


  @JIRA-STORY:PO-421 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5717
  Scenario: Different major creditors return different identity data
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000076
    Then the response does not contain the below major creditor data
      | major_creditor_id   | 1300000000075               |
      | name                | LORD CHANCELLORS DEPARTMENT |
      | major_creditor_code | 0096                        |
