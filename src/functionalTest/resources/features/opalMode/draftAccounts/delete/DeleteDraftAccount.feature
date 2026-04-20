@Opal @JIRA-LABEL:manual-account-creation @JIRA-STORY:PO-2117 @JIRA-EPIC:PO-2744 @JIRA-LABEL:test-support-endpoint-test
Feature: Delete Draft Account


  @JIRA-KEY:POT-6052
  Scenario: Delete the just-created draft account with concurrency control
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    And I store the created draft account ID

    Then I delete the last created draft account using concurrency control

  @cleanUpData @JIRA-KEY:POT-6055
  Scenario: Cleanup should not fail if the account is already gone
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    And I store the created draft account ID

    # Delete it once (should remove it)
    When I delete the last created draft account using concurrency control

    # Try deleting again, but tolerate 404 by setting ignore_missing=true
    When I delete the last created draft account ignoring missing resource

