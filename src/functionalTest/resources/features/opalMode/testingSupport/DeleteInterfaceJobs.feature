@Opal @JIRA-EPIC:PO-2468 @JIRA-STORY:PO-2578 @R1C
Feature: Delete interface jobs for test support

  # E2E.02 proves the deployed Create, GET summary, and Delete APIs work together. The
  # integration test proves deletion of the message, till, and payment-in database records.

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  Scenario: E2E.01 Testing-support interface-job deletion is available in an enabled non-production environment
    When I request deletion of an interface job in the enabled E2E environment
    Then the request succeeds

  Scenario: E2E.02 Deleting a created interface job removes it from the summary
    Given an isolated interface job has been created
    And the created interface job is returned by the interface-jobs summary API
    When I delete the created interface job using testing support
    Then the request succeeds
    And the created interface job is no longer returned by the interface-jobs summary API
