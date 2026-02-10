Feature: Defendant Accounts Search API (consolidation_search)

  @Opal @PO-2296
  Scenario: The Defendant Accounts Search API returns consolidation fields when consolidation_search=true
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
  Scenario: The Defendant Accounts Search API returns 401 for an invalid token
    Given I set an invalid token
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
   Then the response from the defendant account search api is unauthorised

  @Opal @PO-2296
  Scenario: The Defendant Accounts Search API returns 403 when the user has no permission
    Given I am testing as the "opal-test-2@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
    Then the response from the defendant account search api is forbidden

  @Opal @PO-2296
  Scenario: The Defendant Accounts Search API does not return consolidation fields when consolidation_search=false
    Given I am testing as the "opal-test@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id    | 77       |
      | account_number      | 12345678 |
      | consolidation_search| false    |
    Then the response status code is 200
    And the response content type is "application/json"
    And the defendant account search response does not include consolidation fields

  @Opal @PO-2296
  Scenario: The Defendant Accounts Search API returns only documented fields when consolidation_search=true
    Given I am testing as the "opal-test@hmcts.net" user
    When I search defendant accounts with consolidation_search true using:
      | business_unit_id | 77       |
      | account_number   | 12345678 |
    Then the response status code is 200
    And the response content type is "application/json"
    And the defendant account search response includes consolidation fields


