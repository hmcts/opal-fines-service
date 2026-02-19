@Opal
Feature: PO-691 post draft account error handling

  @PO-691 @cleanUpData @JIRA-KEY:POT-195
  Scenario: Post draft account - CEP1 - Invalid Request Payload
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id_ | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 400

  @PO-691 @cleanUpData @JIRA-KEY:POT-196
  Scenario: Post draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to create a draft account with an invalid token using created by ID "BUUID"
    Then The draft account response returns 401

  @PO-691 @cleanUpData @JIRA-KEY:POT-197
  Scenario: Post draft account - CEP4 - Resource Not Found
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to hit an endpoint that doesn't exist
    Then The draft account response returns 404


  @PO-691 @cleanUpData @JIRA-KEY:POT-198
  Scenario: Post draft account - CEP5 - Unsupported Content Type
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to create a draft account with an unsupported content type
    Then The draft account response returns 406

  @PO-691 @cleanUpData @JIRA-KEY:POT-199
  Scenario: Post draft account - CEP7 - Unsupported Media Type
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to create a draft account with an unsupported media type
    Then The draft account response returns 415

  @PO-691 @cleanUpData @JIRA-KEY:POT-200
  Scenario: Post draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa             |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 500
