Feature: Defendant account search - consolidation search

  @Opal @PO-2296
  Scenario: consolidation_search=true returns consolidation fields
    When I search defendant accounts with consolidation_search true using:
      | active_accounts_only | true     |
      | business_unit_id     | 77       |
      | account_number       | 12345678 |
      | organisation         | false    |
    Then the response status code is 200
    And the response content type is "application/json"
    And the response contains consolidation fields for the first result
    And the first result has error reference "CON.ER.4"

  @Opal @PO-2296
  Scenario: Defendant account search - Invalid Auth
    Given I set an invalid token
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
  Then the response from the defendant account search api is unauthorised

  @Opal @PO-2296
  Scenario: Defendant account search - No Permission returns 403
    Given I am testing as the "opal-test-2@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
    Then the response from the defendant account search api is forbidden

  @Opal @PO-2296
  Scenario: consolidation_search=false returns only base documented fields
    Given I am testing as the "opal-test@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id    | 77       |
      | account_number      | 12345678 |
      | consolidation_search| false    |
    Then the response status code is 200
   And the response content type is "application/json"
    And the defendant account search response does not include consolidation fields

  @Opal @PO-2296
  Scenario: consolidation_search=true returns only documented fields (includes consolidation fields)
    Given I am testing as the "opal-test@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
    Then the response status code is 200
    And the response content type is "application/json"
    And the defendant account search response includes consolidation fields


