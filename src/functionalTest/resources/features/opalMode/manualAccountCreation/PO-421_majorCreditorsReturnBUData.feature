Feature: Major Creditors Api returns Business unit level Data

  @Opal @PO-421
  Scenario: Major Creditors Api returns Business unit level Data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 15
    Then the response contains the below major creditor data
      | major_creditor_id   | 15                           |
      | name                | CHESTERFIELD BOROUGH COUNCIL |
      | major_creditor_code | CHBC                         |
      | business_unit_id    | 10                           |


  @Opal @PO-421
  Scenario: Major Creditors Api returns Business unit level Data - negative test
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the major creditors ref data api filter by major creditor id 109
    Then the response does not contain the below major creditor data
      | major_creditor_id   | 15                           |
      | name                | CHESTERFIELD BOROUGH COUNCIL |
      | major_creditor_code | CHBC                         |
      | business_unit_id    | 10                           |
