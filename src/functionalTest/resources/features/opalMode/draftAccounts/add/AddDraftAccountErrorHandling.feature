@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Add Draft Account Error Handling

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5628
  Scenario: Creating a draft account with invalid data is rejected
    When I create a draft account with the following details
      | business_unit_id_ | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then the request is rejected as bad request

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5629
  Scenario: Creating a draft account without a valid access token is rejected
    When I attempt to create a draft account with an invalid token using created by ID "BUUID"
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5630
  Scenario: Creating a draft account against a missing endpoint is rejected
    When I attempt to hit an endpoint that doesn't exist
    Then the request is rejected as not found

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5631
  Scenario: Creating a draft account with an unsupported response content type is rejected
    When I attempt to create a draft account with an unsupported content type
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5632
  Scenario: Creating a draft account with an unsupported request media type is rejected
    When I attempt to create a draft account with an unsupported media type
    Then the request is rejected as unsupported media type

  @JIRA-STORY:PO-691 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5633
  Scenario: Creating a draft account with malformed values is rejected
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa             |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then the request is rejected as bad request
