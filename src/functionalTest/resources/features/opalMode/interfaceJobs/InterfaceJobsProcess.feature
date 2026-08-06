@Opal @R1CPayment @JIRA-LABEL:interface-jobs @JIRA-EPIC:PO-2468
Feature: Process interface jobs

  # Temporarily ignored because the deployed Service Bus queue/emulator is unavailable;
  # re-enable when the queue dependency is available.
  @Ignore @JIRA-STORY:PO-2593 @JIRA-TEST-KEY:PO-2593-E2E-01
  Scenario: Eligible interface jobs are accepted for processing
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create two eligible interface jobs for processing
    When I submit the eligible interface jobs for processing
    Then the process response is 200 with an empty body
    And the eligible jobs are updated in the database
    And the eligible jobs are present on the process-interface-files queue

  # Temporarily ignored because scenario setup publishes to the unavailable Service Bus queue;
  # re-enable when the queue dependency is available.
  @Ignore @JIRA-STORY:PO-2593 @JIRA-TEST-KEY:PO-2593-E2E-02
  Scenario: Mixed interface-job statuses return conflict without partial processing
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create an interface job that has already begun processing and an unprocessed interface job
    When I submit the mixed-status interface jobs for processing
    Then the request is rejected as conflict
    And the unprocessed mixed-status job remains unchanged
    And the unprocessed mixed-status job is not present on the process-interface-files queue

  @JIRA-STORY:PO-2593 @JIRA-TEST-KEY:PO-2593-E2E-03
  Scenario: Missing access token is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit the eligible interface jobs for processing without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2593 @JIRA-TEST-KEY:PO-2593-E2E-03
  Scenario: Invalid access token is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit the eligible interface jobs for processing with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2593 @JIRA-TEST-KEY:PO-2593-E2E-03
  Scenario: User without payment processing permission is forbidden
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create two eligible interface jobs for processing
    When a user without payment processing permission submits the eligible interface jobs for processing
    Then the request is rejected as forbidden
