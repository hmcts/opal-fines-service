@Opal @R1CPayment @JIRA-LABEL:interface-jobs @JIRA-LABEL:personal-data-processing-logging @JIRA-EPIC:PO-2468
Feature: Get processed file summary

  @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-01
  Scenario: Reject request without an access token
    When I call GET "/interface-jobs/999999999/processed-file-summary" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-01
  Scenario: Reject request with an invalid access token
    When I call GET "/interface-jobs/999999999/processed-file-summary" with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-02
  Scenario: Permitted user retrieves a processed file summary and emits a PDPO log
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create an interface job for processed file summary
    And I submit the interface job for processing
    And I wait for the interface job to complete
    When I request the processed file summary for the completed interface job
    Then the response status code is 200
    And the response matches the processed file summary schema
    And the processed file summary contains the expected file details and totals
    And interface messages are grouped by message text
    And the logging service emits a PDPO log for the processed file summary

  @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-03
  Scenario: Unknown interface job returns not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request GET "/interface-jobs/999999999/processed-file-summary"
    Then the request is rejected as not found

  @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-01
  Scenario: User without payment processing permission is forbidden
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create an interface job for processed file summary
    And I submit the interface job for processing
    And I wait for the interface job to complete
    When I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    And I request the processed file summary for the completed interface job
    Then the request is rejected as forbidden

  @R1CPaymentOff @JIRA-STORY:PO-2576 @JIRA-TEST-KEY:PO-2576-E2E-06
  Scenario: Processed file summary is unavailable when payment processing is disabled
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request GET "/interface-jobs/999999999/processed-file-summary"
    Then the response reports that the feature is disabled
