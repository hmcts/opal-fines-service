@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Get Draft Account Error Handling

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4484
  Scenario: Get draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I attempt to get a draft account with an invalid token
    Then The draft account response returns 401

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4485
  Scenario: Get draft account - CEP4 - Resource Not Found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I attempt to hit an endpoint that doesn't exist
    Then The draft account response returns 404


  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4486
  Scenario: Get draft account - CEP5 - Unsupported Content Type
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
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

  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4487
  Scenario: Get draft account - CEP5 - Unsupported Content Type in Url parameter
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I get the draft account "not A Long"
    Then The draft account response returns 406


  @JIRA-STORY:PO-690 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4488
  Scenario: Get draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I get the draft account trying to provoke an internal server error
    Then The draft account response returns 500
