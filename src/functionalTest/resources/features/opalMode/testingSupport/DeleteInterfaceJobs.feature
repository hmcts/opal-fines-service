@Opal @Ignore @JIRA-EPIC:PO-2468 @JIRA-STORY:PO-2578 @R1C
Feature: Delete interface jobs for test support

  # Enable these scenarios when the Interface Jobs Add and Process APIs are available in the
  # deployed E2E environment. The setup must create an isolated job with an interface file,
  # message, till, and payment-in record, and return the created interface job ID.

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  Scenario: E2E.01 Testing-support interface-job deletion is available only in an enabled non-production environment
    When I request deletion of an interface job in the enabled E2E environment
    Then the request succeeds
    # The matching disabled-environment check must run against an explicitly configured
    # production-like deployment and return 404. It cannot share this environment's TEST_URL.

  Scenario: E2E.02 Deleting an interface job removes its complete processed payment data
    Given an isolated interface job with related payment data has been created and processed
    When I delete the created interface job using testing support
    Then the request succeeds
    And the created interface job is no longer returned by the interface-jobs summary API
    # The integration test proves deletion of interface_messages, tills, and payments_in.
    # The E2E check proves the deployed Add, Process, GET, and DELETE journey end to end.

  Scenario: E2E.03 Testing-support interface-job deletion enforces authentication and authorisation
    When I delete an interface job without an access token
    Then the request is rejected as unauthorized
    When I delete an interface job with an invalid access token
    Then the request is rejected as unauthorized
    When a user without Process and Allocate Payments permission deletes an interface job
    Then the request is rejected as forbidden
