@Opal @JIRA-LABEL:reference-data @JIRA-LABEL:authentication-authorisation
Feature: Central Funds Reference Data

  # Ignored because deployed environments do not currently contain the Central Fund
  # configuration data needed for a successful GET /central-funds/73 response.
  @Ignore
  @JIRA-STORY:PO-2320 @JIRA-EPIC:PO-1286
  Scenario: A central fund can be retrieved by business unit identifier
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the central funds api for business unit 73
    Then the central fund response matches the deployed contract

  @JIRA-STORY:PO-2320 @JIRA-EPIC:PO-1286 @JIRA-TEST-KEY:PO-7856
  Scenario: A central fund request without a token is rejected
    When I call GET "/central-funds/73" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2320 @JIRA-EPIC:PO-1286 @JIRA-TEST-KEY:PO-7857
  Scenario: A central fund request with an invalid token is rejected
    When I call GET "/central-funds/73" with an invalid token
    Then the request is rejected as unauthorized

  # Ignored because the forbidden-path check also depends on a real deployed Central Fund row,
  # and that configuration data is not currently present in the database.
  @Ignore
  @JIRA-STORY:PO-2320 @JIRA-EPIC:PO-1286
  Scenario: A central fund request without Search and View Accounts permission is forbidden
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I make a request to the central funds api for business unit 73
    Then the central fund forbidden response is returned

  @JIRA-STORY:PO-2320 @JIRA-EPIC:PO-1286 @JIRA-TEST-KEY:PO-7858
  Scenario: An unknown central fund business unit returns not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the central funds api for business unit 999
    Then the central fund not found response is returned
