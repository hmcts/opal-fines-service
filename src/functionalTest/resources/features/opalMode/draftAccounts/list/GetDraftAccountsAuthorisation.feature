@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation @R1A
Feature: Get Draft Accounts Authorisation

  @JIRA-STORY:PO-829 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5662
  Scenario: Get Draft Accounts - No Permission
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 78                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    #the test user2 doesn't have permission to business unit id 78
    When the "opal-test-2@dev.platform.hmcts.net" user attempts to list draft accounts for business unit "78"
    Then the request is rejected as forbidden

  @JIRA-STORY:PO-829 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5663
  Scenario: Get Draft Accounts - account created in BU requesting user doesn't have permission to
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | 80               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk

    When I am testing as the "opal-test-3@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    When the "opal-test-10@dev.platform.hmcts.net" user requests visible draft accounts
    Then the visible draft accounts exclude business units "73, 26"
