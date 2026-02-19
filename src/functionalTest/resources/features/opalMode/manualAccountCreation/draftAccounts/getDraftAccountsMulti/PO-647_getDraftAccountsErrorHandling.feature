@Opal
Feature: PO-647 get draft accounts error handling

  @PO-647 @cleanUpData @JIRA-KEY:POT-211
  Scenario: Get draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to get draft accounts with an invalid token
    Then The draft account response returns 401

  @PO-647 @cleanUpData @JIRA-KEY:POT-212
  Scenario: Get draft account - CEP5 - Unsupported Content Type
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    |                                        |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
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

  @PO-647 @cleanUpData @JIRA-KEY:POT-213
  Scenario: Get draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@hmcts.net" user
    When I get the draft accounts trying to provoke an internal server error
    Then The draft account response returns 400
