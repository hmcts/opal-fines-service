@Opal @JIRA-LABEL:authentication-authorisation @JIRA-STORY:PO-896 @JIRA-EPIC:PO-2233
Feature: Authentication And Authorisation Parity

  @JIRA-STORY:PO-896 @JIRA-EPIC:PO-2233 @JIRA-KEY:POT-4459
  Scenario: Expired token returns 401
    Given I am testing with an expired token for the "opal-test-10@dev.platform.hmcts.net" user
    When I make a raw request to the business unit ref data api filtering by business unit type "area"
    Then the response status is 401
