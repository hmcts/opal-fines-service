@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Get Draft Account Authorisation

  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Get Draft Account - No Permission
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    When the "opal-test-2@dev.platform.hmcts.net" user attempts to view the created draft account
    Then access to the created draft account is denied


  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData
    ### This test is currently ignored as the permissions are not quite right for this test to pass.

  Scenario: Get Draft Account - No Permission in same BU
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    When the "opal-test-10@dev.platform.hmcts.net" user attempts to view the created draft account
    Then access to the created draft account is denied

  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Get Draft Account - Permission in different BU
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    When the "opal-test-8@dev.platform.hmcts.net" user attempts to view the created draft account
    Then access to the created draft account is denied
