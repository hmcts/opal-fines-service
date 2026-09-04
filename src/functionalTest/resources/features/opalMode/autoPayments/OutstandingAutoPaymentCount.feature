@Opal @R1CPayment @JIRA-LABEL:auto-payments @JIRA-EPIC:PO-2468
Feature: Outstanding auto payment counts

  @JIRA-STORY:PO-2470 @JIRA-TEST-KEY:PO-2470-E2E-01
  Scenario: Missing access token is rejected when retrieving outstanding auto payment counts
    When I call GET "/business-units/outstanding-auto-payment-count" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2470 @JIRA-TEST-KEY:PO-2470-E2E-01
  Scenario: Invalid access token is rejected when retrieving outstanding auto payment counts
    When I call GET "/business-units/outstanding-auto-payment-count" with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2470 @JIRA-TEST-KEY:PO-2470-E2E-01
  Scenario: Authenticated user can retrieve outstanding auto payment counts
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request outstanding auto payment counts
    Then the outstanding auto payment count response is successful

  @JIRA-STORY:PO-2470 @JIRA-TEST-KEY:PO-2470-E2E-02
  Scenario: Outstanding auto payment counts are read from the deployed view
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create two eligible interface jobs for processing
    And the seeded interface jobs are visible in the summary API
    When I request outstanding auto payment counts
    Then the outstanding auto payment count response conforms to the documented schema
    And business unit 78 has outstanding files to process

  @JIRA-STORY:PO-2470 @JIRA-TEST-KEY:PO-2470-E2E-03
  Scenario: User without payment processing permission receives an empty response
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request outstanding auto payment counts
    Then the outstanding auto payment count response is empty
