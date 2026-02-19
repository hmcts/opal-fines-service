@Opal
Feature: PO-690 get draft account error handling

  @PO-690 @cleanUpData @JIRA-KEY:POT-217
  Scenario: Get draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to get a draft account with an invalid token
    Then The draft account response returns 401

  @PO-690 @cleanUpData @JIRA-KEY:POT-218
  Scenario: Get draft account - CEP4 - Resource Not Found
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to hit an endpoint that doesn't exist
    Then The draft account response returns 404


  @PO-690 @cleanUpData @JIRA-KEY:POT-219
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

    When I attempt to get a draft account with an unsupported content type
    Then The draft account response returns 406

  @PO-690 @cleanUpData @JIRA-KEY:POT-220
  Scenario: Get draft account - CEP5 - Unsupported Content Type in Url parameter
    Given I am testing as the "opal-test@hmcts.net" user
    When I get the draft account "not A Long"
    Then The draft account response returns 406


  @PO-690 @cleanUpData @JIRA-KEY:POT-221
  Scenario: Get draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@hmcts.net" user
    When I get the draft account trying to provoke an internal server error
    Then The draft account response returns 500
