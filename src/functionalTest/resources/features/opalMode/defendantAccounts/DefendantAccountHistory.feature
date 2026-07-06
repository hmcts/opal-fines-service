@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account History

  @cleanUpData @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621 @JIRA-TEST-KEY:PO-8660
  Scenario: E2E.01 Happy path history retrieval
    Given a defendant account with history exists for submitted by "DEFHST001"
    When I request defendant account history for the created defendant account
    Then the defendant account history response is returned as documented
    And the defendant account history contains at least the following item counts
      | Amendment     | 1 |
      | Enforcement   | 1 |
      | Financial     | 1 |
      | Note          | 1 |
      | Payment terms | 1 |
    And the defendant account history contains seeded amendment history
    And the defendant account history contains seeded enforcement history
    And the defendant account history is ordered newest first

  @cleanUpData @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621 @JIRA-TEST-KEY:PO-8661
  Scenario: E2E.02 Filter contract and idempotence
    Given a defendant account with history exists for submitted by "DEFHST002"
    When I request defendant account history for the created defendant account
    Then the defendant account history response is returned as documented
    And I remember the returned defendant account history date range
    When I request defendant account history for the created defendant account using the remembered dateFrom boundary
    Then the defendant account history response contains only items on or after the remembered dateFrom
    And the defendant account history response includes an item on the remembered dateFrom
    When I request defendant account history for the created defendant account using the remembered dateTo boundary
    Then the defendant account history response contains only items on or before the remembered dateTo
    And the defendant account history response includes an item on the remembered dateTo
    When I request defendant account history for the created defendant account with query "itemTypes=ENFORCEMENT"
    Then the defendant account history contains only the following item types
      | Enforcement |
    And the defendant account history contains seeded enforcement history
    When I request defendant account history for the created defendant account with query "itemTypes=enforcement,note,paymentTerms"
    Then the defendant account history contains only the following item types
      | Enforcement   |
      | Note          |
      | Payment terms |
    When I request defendant account history for the created defendant account using the remembered date range and itemTypes "enforcement,note,paymentTerms"
    Then the defendant account history contains only the following item types
      | Enforcement   |
      | Note          |
      | Payment terms |
    And the defendant account history response contains only items on or after the remembered dateFrom
    And the defendant account history response contains only items on or before the remembered dateTo
    When I request defendant account history for the created defendant account using the remembered date range and itemTypes "enforcement,note,paymentTerms" twice
    Then the repeated defendant account history responses are identical

  @cleanUpData @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621 @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-8662
  Scenario: E2E.03 Authentication and authorization
    Given a defendant account with history exists for submitted by "DEFHST003"
    When I request defendant account history for the created defendant account without a token
    Then the defendant account history error response matches the standard problem detail contract for status 401
    When the "opal-test-2@dev.platform.hmcts.net" user requests defendant account history for the created defendant account
    Then the defendant account history error response matches the standard problem detail contract for status 403
    When the "opal-test@dev.platform.hmcts.net" user requests defendant account history for the created defendant account
    Then the defendant account history request succeeds

  @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621 @JIRA-TEST-KEY:PO-8663
  Scenario: E2E.04 Not found behaviour
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request defendant account history for a non-existent defendant account
    Then the defendant account history error response matches the standard problem detail contract for status 404
    And the defendant account history error response does not leak internal details
