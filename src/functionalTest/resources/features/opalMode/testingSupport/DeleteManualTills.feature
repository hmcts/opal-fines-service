@Opal @R1CPayment @JIRA-EPIC:PO-2439 @JIRA-STORY:PO-10810
Feature: Delete manual tills for test support

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  # AC1 — Endpoint availability in an enabled non-production test environment.
  @JIRA-STORY:PO-10810
  Scenario: E2E.01 Testing-support manual-till deletion is available in an enabled non-production environment
    When I delete a non-existent manual till using testing support
    Then the request succeeds

  # AC1 & AC6 — Deletes a manually created till and its linked payments.
  @JIRA-STORY:PO-10810
  Scenario: E2E.02 Deleting a manually created till removes its linked payments
    Given I record the existing till identifiers for business unit 78
    When I submit a uniquely identifiable valid request to create a till with one criminal payment
    Then the request creates a resource
    And I can retrieve the newly created till
    And the till belongs to business unit 78 and contains one linked criminal payment
    When I delete the created manual till using testing support
    Then the request succeeds
    And the created manual till is no longer available

  # AC2 — Deletes only the explicitly supplied manual till ID.
  @JIRA-STORY:PO-10810
  Scenario: E2E.03 Deleting one manually created till preserves another
    Given I create a manual till as unrelated data for business unit 78
    And I record the existing till identifiers for business unit 78
    When I submit a uniquely identifiable valid request to create a till with one criminal payment
    Then I can retrieve the newly created till
    And I delete the created manual till using testing support
    Then the request succeeds
    And the unrelated manual till remains available

  # AC3 & AC4 — Supports multiple, repeated, and absent IDs.
  @JIRA-STORY:PO-10810
  Scenario: E2E.04 Deleting multiple manual tills tolerates repeated and absent IDs
    Given I create a manual till as unrelated data for business unit 78
    And I record the existing till identifiers for business unit 78
    When I submit a uniquely identifiable valid request to create a till with one criminal payment
    Then I can retrieve the newly created till
    And I delete both created manual tills using testing support with repeated and absent IDs
    Then the request succeeds
    And the created manual till is no longer available
    And the unrelated manual till is no longer available

  # AC4 — Rejects an empty IDs value with the standard validation error.
  @JIRA-STORY:PO-10810
  Scenario: E2E.05 Deleting manual tills with an empty IDs value returns a standard error
    When I delete manual tills using testing support with an empty IDs value
    Then the manual-till deletion error matches the standard problem detail contract for status 400

  # AC4 — Rejects a malformed IDs value using the standard type-mismatch error.
  @JIRA-STORY:PO-10810
  Scenario: E2E.06 Deleting manual tills with a malformed IDs value returns a standard error
    When I delete manual tills using testing support with a malformed IDs value
    Then the manual-till deletion error matches the standard problem detail contract for status 406
