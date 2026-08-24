@Opal @JIRA-LABEL:authentication-authorisation @R1A
Feature: Authentication And Authorisation Parity

  @JIRA-STORY:PO-896 @JIRA-EPIC:PO-2233 @JIRA-TEST-KEY:PO-5619
  Scenario: Expired access tokens are rejected
    Given I am testing with an expired token for the "opal-test-10@dev.platform.hmcts.net" user
    When I make a raw request to the business unit ref data api filtering by business unit type "area"
    Then the request is rejected as unauthorized
