@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Add Draft Account Authorisation

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - Invalid Auth
    Given I set an invalid token
    When I create a draft account with the following details using a raw HTTP client
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - No Permission
    When the "opal-test-2@dev.platform.hmcts.net" user attempts to create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    Then the request is rejected as forbidden

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - Permission in different BU
    When the "opal-test@dev.platform.hmcts.net" user attempts to create a draft account with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    Then the request is rejected as forbidden
