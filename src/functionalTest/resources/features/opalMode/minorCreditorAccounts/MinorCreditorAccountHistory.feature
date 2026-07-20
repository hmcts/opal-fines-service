@Opal @JIRA-LABEL:account-enquiry @MinorCreditorHistory
Feature: Minor Creditor Account History

  @cleanUpData @JIRA-STORY:PO-2642 @JIRA-EPIC:PO-2653
  Scenario: E2E.01 Happy path history retrieval
    Given a minor creditor account with representative history exists for submitted by "MCHIST001"
    When I request minor creditor account history for the created minor creditor account
    Then the minor creditor account history response is returned as documented
    And the minor creditor account history contains at least the following item counts
      | Amendment | 3 |
      | Financial | 3 |
      | Note      | 3 |
    And the minor creditor account history is ordered newest first

  @JIRA-STORY:PO-2642 @JIRA-EPIC:PO-2653 @JIRA-NFR:PO-2507
  Scenario: E2E.02 Authentication
    When I request minor creditor account history for a non-existent minor creditor account without a token
    Then the minor creditor account history error response matches the standard problem detail contract for status 401
    And the minor creditor account history error response contains no account data
    When I request minor creditor account history for a non-existent minor creditor account with an invalid token
    Then the minor creditor account history error response matches the standard problem detail contract for status 401
    And the minor creditor account history error response contains no account data

  @cleanUpData @JIRA-STORY:PO-2642 @JIRA-EPIC:PO-2653
  Scenario: E2E.03 Authorization
    Given a minor creditor account with representative history exists for submitted by "MCHIST003"
    When the "opal-test-2@dev.platform.hmcts.net" user requests minor creditor account history for the created minor creditor account
    Then the minor creditor account history error response matches the standard problem detail contract for status 403
    And the minor creditor account history error response contains no account data

  @JIRA-STORY:PO-2642 @JIRA-EPIC:PO-2653
  Scenario: E2E.04 Unknown creditor
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request minor creditor account history for a non-existent minor creditor account
    Then the minor creditor account history error response matches the standard problem detail contract for status 404
    And the minor creditor account history error response contains no account data

  @cleanUpData @JIRA-STORY:PO-2642 @JIRA-EPIC:PO-2653
  Scenario: E2E.05 Combined filters
    Given a minor creditor account with representative history exists for submitted by "MCHIST005"
    When I request minor creditor account history for the created minor creditor account
    Then the minor creditor account history response is returned as documented
    And the minor creditor account history includes records outside the remembered date range
    When I request minor creditor account history for the created minor creditor account using the remembered date range and itemTypes "amendment,note"
    Then the minor creditor account history response contains only items on or after the remembered dateFrom
    And the minor creditor account history response contains only items on or before the remembered dateTo
    And the minor creditor account history excludes records outside the remembered date range
    And the minor creditor account history contains only the following item types
      | Amendment |
      | Note      |
    And the minor creditor account history excludes the following item types
      | Financial |
