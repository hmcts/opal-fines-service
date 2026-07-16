@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Payment Card Request

  @JIRA-STORY:PO-6449 @JIRA-EPIC:PO-977 @JIRA-TEST-KEY:PO-6449
  Scenario: Payment card request requires the business unit header
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request a defendant account payment card without the Business-Unit-Id header
    Then the request is rejected with status 400
    And the payment card request response reports the missing Business-Unit-Id header
