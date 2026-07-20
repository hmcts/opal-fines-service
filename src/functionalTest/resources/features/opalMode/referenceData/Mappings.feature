@Opal @JIRA-LABEL:reference-data
Feature: Mappings Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1B
  Scenario: A mappings request without a token is rejected
    When I call GET "/mappings/defendant-account-status" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1B
  Scenario: A mappings request with an invalid token is rejected
    When I call GET "/mappings/defendant-account-status" with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1B
  Scenario: Defendant account status mappings can be retrieved
    When I make a request to the mappings api for type "defendant-account-status"
    Then the defendant account status mappings are returned

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1B
  Scenario: A mappings request with an unsupported type is rejected as bad request
    When I make a request to the mappings api for type "unsupported-type"
    Then the request is rejected as bad request

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1B
  Scenario: A mappings request without a type is rejected as bad request
    When I make a request to the mappings api without a type
    Then the request is rejected as bad request
