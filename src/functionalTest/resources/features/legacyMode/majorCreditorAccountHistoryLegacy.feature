@Legacy @JIRA-LABEL:account-enquiry @MajorCreditorHistory
Feature: Major Creditor Account History API In Legacy Mode

  @JIRA-STORY:PO-2659 @JIRA-EPIC:PO-2655
  Scenario: E2E.01 Happy path history retrieval
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request major creditor account history for the created major creditor account
    Then the major creditor account history response is returned as documented

  @JIRA-STORY:PO-2659 @JIRA-EPIC:PO-2655 @JIRA-NFR:PO-2507
  Scenario: E2E.02 Authentication propagation returns 401 when authentication is missing or invalid
    When I request major creditor account history for the created major creditor account without a token
    Then the major creditor account history error response matches the standard problem detail contract for status 401
    And the major creditor account history error response contains no account data
    When I request major creditor account history for the created major creditor account with an invalid token
    Then the major creditor account history error response matches the standard problem detail contract for status 401
    And the major creditor account history error response contains no account data

  @JIRA-STORY:PO-2659 @JIRA-EPIC:PO-2655 @JIRA-NFR:PO-2507
  Scenario: E2E.03 Authorization propagation returns 403 when permission is missing
    When the "opal-test-2@dev.platform.hmcts.net" user requests major creditor account history for the created major creditor account
    Then the major creditor account history error response matches the standard problem detail contract for status 403
    And the major creditor account history error response contains no account data

  @JIRA-STORY:PO-2659 @JIRA-EPIC:PO-2655
  Scenario: E2E.04 Unknown creditor returns 404
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request major creditor account history for a non-existent major creditor account
    Then the major creditor account history error response matches the standard problem detail contract for status 404
    And the major creditor account history error response contains no account data

  @JIRA-STORY:PO-2659 @JIRA-EPIC:PO-2655
  Scenario: E2E.05 Filter contract and idempotence
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request major creditor account history for the created major creditor account with query "dateFrom=2020-01-01"
    Then the major creditor account history response is returned as documented
    And I remember the returned major creditor account history date range
    When I request major creditor account history for the created major creditor account with query "dateFrom=2020-01-01&dateTo=2099-12-31&itemTypes=financial"
    Then the major creditor account history response is returned as documented
    And the major creditor account history response contains only items on or after the remembered dateFrom
    And the major creditor account history response contains only items on or before the remembered dateTo
    And the major creditor account history contains only the following item types
      | Financial |
    When I request major creditor account history for the created major creditor account twice
    Then the repeated major creditor account history responses are identical
