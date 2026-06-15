@Opal @JIRA-LABEL:manual-account-creation @JIRA-STORY:PO-2117 @JIRA-EPIC:PO-2141 @JIRA-LABEL:test-support-endpoint-test
Feature: Delete Draft Account

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @JIRA-TEST-KEY:PO-5635
  Scenario: A draft account can be deleted using optimistic locking
    Given a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |

    When I delete the created draft account using optimistic locking
    Then the created draft account is deleted

  @cleanUpData @JIRA-TEST-KEY:PO-5636
  Scenario: Cleanup ignores a draft account that has already been deleted
    Given a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |

    When I delete the created draft account using optimistic locking
    And I delete the created draft account again, ignoring a missing resource
    Then the created draft account is deleted
