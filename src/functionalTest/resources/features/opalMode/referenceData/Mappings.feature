@Opal @JIRA-LABEL:reference-data
Feature: Mappings Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372
  Scenario: Defendant account status mappings can be retrieved
    When I make a request to the mappings api for type "defendant-account-status"
    Then the defendant account status mappings are returned

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372
  Scenario: A mappings request with an unsupported type is rejected as bad request
    When I make a request to the mappings api for type "unsupported-type"
    Then the request is rejected as bad request

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372
  Scenario: A mappings request without a type is rejected as bad request
    When I make a request to the mappings api without a type
    Then the request is rejected as bad request
