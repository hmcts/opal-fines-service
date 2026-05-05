@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Get Draft Account Error Handling

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Retrieving a draft account without a valid access token is rejected
    When I attempt to get a draft account with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Retrieving a draft account from a missing endpoint is rejected
    When I attempt to hit an endpoint that doesn't exist
    Then the request is rejected as not found

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Retrieving a draft account with an unsupported response content type is rejected
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    |                                         |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    When I attempt to get a draft account with an unsupported content type
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Retrieving a draft account with an invalid identifier is rejected
    When I get the draft account "not A Long"
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Retrieving a draft account with a malformed request fails
    When I get the draft account trying to provoke an internal server error
    Then the request fails with an internal server error
