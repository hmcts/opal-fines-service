@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Replace Draft Account Error Handling

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a draft account with invalid data is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I attempt to put a draft account with an invalid request payload
      | business_unit_id  |                                         |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    Then the request is rejected as bad request

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a draft account without a valid access token is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I set an invalid token
    And I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a missing draft account is rejected
    When I attempt to put a draft account with resource not found
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | If-Match          | 0                                       |

    Then the request is rejected as not found

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a draft account with an unsupported response content type is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I attempt to put a draft account with unsupported content type for response
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a draft account with an unsupported request format is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I attempt to put a draft account with unsupported media type for request

    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-749 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Replacing a draft account with a malformed request fails
    When I put the draft account trying to provoke an internal server error
    Then the request fails with an internal server error
