@Legacy @R1BDrop2 @JIRA-LABEL:account-enquiry @MinorCreditorAtAGlance
Feature: Minor Creditor Account At A Glance API In Legacy Mode

  @JIRA-STORY:PO-10678 @JIRA-EPIC:PO-2982
  Scenario: E2E.01 Legacy minor creditor account at a glance returns organisation defendant details
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request minor creditor account at a glance for the seeded legacy minor creditor account
    Then the minor creditor account at a glance response is returned as documented
    And the minor creditor account at a glance defendant is an organisation named "Parking Enforcement Corp"

  @JIRA-STORY:PO-10678 @JIRA-EPIC:PO-2982
  Scenario: E2E.02 Legacy minor creditor account at a glance rejects a request without authentication
    When I request minor creditor account at a glance for the seeded legacy minor creditor account without a token
    Then the minor creditor account at a glance error response matches the standard problem detail contract for status 401

  @JIRA-STORY:PO-10678 @JIRA-EPIC:PO-2982
  Scenario: E2E.03 Legacy minor creditor account at a glance rejects a user without permission
    When the "opal-test-2@dev.platform.hmcts.net" user requests minor creditor account at a glance for the seeded legacy minor creditor account
    Then the minor creditor account at a glance error response matches the standard problem detail contract for status 403

  @JIRA-STORY:PO-10678 @JIRA-EPIC:PO-2982
  Scenario: E2E.04 Legacy minor creditor account at a glance returns not found for an unknown account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request minor creditor account at a glance for a non-existent minor creditor account
    Then the minor creditor account at a glance error response matches the standard problem detail contract for status 404
