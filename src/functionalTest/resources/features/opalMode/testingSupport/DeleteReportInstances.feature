@Opal @R1C @JIRA-EPIC:PO-2468 @JIRA-STORY:PO-10660
Feature: Delete report instances for test support

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  # AC1 — Endpoint availability in an enabled non-production test environment.
  Scenario: E2E.01 Testing-support report-instance deletion is available in an enabled non-production environment
    When I delete a non-existent report instance using testing support
    Then the request succeeds

  # AC2 & AC6 — Deletes a Cash Till report instance and its stored report content.
  @R1CPayment @Ignore @JIRA-DEFECT:PO-10560
  Scenario: E2E.02 Deleting a generated Cash Till report instance removes its content
    Given I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    Then the generated Cash Till report instance content is available
    When I delete the generated Cash Till report instance using testing support
    Then the request succeeds
    And the generated Cash Till report instance is no longer available
    And the generated Cash Till report instance content is no longer available

  # AC3 — Deletes only the explicitly supplied report-instance ID.
  @R1CPayment @Ignore @JIRA-DEFECT:PO-10560
  Scenario: E2E.03 Deleting one generated Cash Till report instance preserves another
    Given I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    Then I retain the generated Cash Till report instance as unrelated data
    Given I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    And I delete the generated Cash Till report instance using testing support
    Then the request succeeds
    And the unrelated Cash Till report instance remains available with its content

  # AC4 — Supports multiple, repeated, and absent report-instance IDs.
  @R1CPayment @Ignore @JIRA-DEFECT:PO-10560
  Scenario: E2E.04 Deleting multiple Cash Till report instances tolerates repeated and absent IDs
    Given I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    Then I retain the generated Cash Till report instance as unrelated data
    Given I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    And I delete both generated Cash Till report instances using testing support with repeated and absent IDs
    Then the request succeeds
    And the generated Cash Till report instance is no longer available
    And the generated Cash Till report instance content is no longer available
    And the unrelated Cash Till report instance is no longer available with its content

  # AC5 — Rejects an empty IDs value with the standard validation error.
  Scenario: E2E.05 Deleting report instances with an empty IDs value returns a standard error
    When I delete report instances using testing support with an empty IDs value
    Then the report-instance deletion error matches the standard problem detail contract for status 400

  # AC5 — Rejects a malformed IDs value using the standard type-mismatch error.
  Scenario: E2E.06 Deleting report instances with a malformed IDs value returns a standard error
    When I delete report instances using testing support with a malformed IDs value
    Then the report-instance deletion error matches the standard problem detail contract for status 406
