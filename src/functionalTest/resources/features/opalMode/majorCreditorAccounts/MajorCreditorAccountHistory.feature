@Opal @JIRA-LABEL:account-enquiry
Feature: Major Creditor Account History

  @JIRA-STORY:PO-2654 @JIRA-EPIC:PO-2233
  Scenario: E2E.01 Happy path history retrieval
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request major creditor account history for the created major creditor account
    Then the major creditor account history response is returned as documented

  @JIRA-STORY:PO-2654 @JIRA-EPIC:PO-2233
  Scenario: E2E.02 Filter contract and idempotence
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

  @JIRA-STORY:PO-2654 @JIRA-EPIC:PO-2233 @JIRA-NFR:PO-2507
  Scenario: E2E.03 Authentication and authorization
    When I request major creditor account history for the created major creditor account without a token
    Then the major creditor account history request is rejected as unauthorized
    When the "opal-test-2@dev.platform.hmcts.net" user requests major creditor account history for the created major creditor account
    Then the major creditor account history request is rejected as forbidden

  @JIRA-STORY:PO-2654 @JIRA-EPIC:PO-2233
  Scenario: E2E.04 Not found behaviour
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request major creditor account history for a non-existent major creditor account
    Then the major creditor account history request is rejected as not found
