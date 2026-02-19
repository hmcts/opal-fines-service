Feature: Major Creditors Api returns Business unit level Data

  @Opal @PO-421 @JIRA-KEY:POT-164
  Scenario: Major Creditors Api returns Business unit level Data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the response contains the below major creditor data
      | major_creditor_id   | 1300000000075               |
      | name                | LORD CHANCELLORS DEPARTMENT |
      | major_creditor_code | 0096                        |
      | business_unit_id    | 130                         |


  @Opal @PO-421 @JIRA-KEY:POT-165
  Scenario: Major Creditors Api returns Business unit level Data - negative test
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000076
    Then the response does not contain the below major creditor data
      | major_creditor_id   | 1300000000075               |
      | name                | LORD CHANCELLORS DEPARTMENT |
      | major_creditor_code | 0096                        |
      | business_unit_id    | 130                         |
