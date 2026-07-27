@Opal @JIRA-LABEL:auto-enforcement-config
Feature: Get Auto Enforcement Config

  @JIRA-STORY:PO-2434 @JIRA-EPIC:PO-2433
  Scenario: E2E.01 – Happy path returns all enforcement account types
    When the "opal-test@dev.platform.hmcts.net" user requests all enforcement account types
    Then all expected enforcement account types should be returned

  @JIRA-STORY:PO-2434 @JIRA-EPIC:PO-2433
  Scenario: E2E.02 – Forbidden without required permission
    When the "opal-test-2@dev.platform.hmcts.net" user requests all enforcement account types
    Then the enforcement account types request should be rejected as forbidden

  @JIRA-STORY:PO-2434 @JIRA-EPIC:PO-2433
  Scenario: E2E.03 – Unauthorized without access token
    When all enforcement account types are requested with an invalid token
    Then the enforcement account types request should be rejected as unauthorized
