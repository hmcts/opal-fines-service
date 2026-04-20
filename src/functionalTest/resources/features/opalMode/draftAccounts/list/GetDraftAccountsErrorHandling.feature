@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Get Draft Accounts Error Handling

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6106
  Scenario: Get draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I attempt to get draft accounts with an invalid token
    Then The draft account response returns 401

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6108
  Scenario: Get draft account - CEP5 - Unsupported Content Type
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    |                                         |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
    Then The draft account response returns 201
    And I store the created draft account ID
    When I create a draft account with the following details
      | business_unit_id  | 65                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I attempt to get draft accounts with an unsupported content type
    Then The draft account response returns 406

  @JIRA-STORY:PO-647 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6109
  Scenario: Get draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I get the draft accounts trying to provoke an internal server error
    Then The draft account response returns 400
