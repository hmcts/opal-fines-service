@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Get Draft Accounts Error Handling

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Listing draft accounts without a valid access token is rejected
    When I attempt to get draft accounts with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Listing draft accounts with an unsupported response content type is rejected
    Given the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/account.json      | Fine         |                | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         |                | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
    When I attempt to get draft accounts with an unsupported content type
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Listing draft accounts with an invalid request is rejected
    When I get the draft accounts trying to provoke an internal server error
    Then the request is rejected as bad request
