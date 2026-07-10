@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Consolidated Accounts

  # NOTE: Blocked until Consolidation 2 can create or seed real consolidated-account data
  # in the deployed functional-test environment.
  @Ignore @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario: E2E.01 Happy path consolidated accounts retrieval
    Given a real master defendant account has consolidated child accounts
    And I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request consolidated accounts for the real master defendant account
    Then the consolidated accounts response contains the expected child account data

  # NOTE: Blocked until the deployed functional-test environment has a stable master
  # defendant account that is known to have no consolidated child accounts.
  @Ignore @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario: E2E.02 Master account without consolidated children returns an empty array
    Given a real master defendant account has no consolidated child accounts
    And I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request consolidated accounts for the real master defendant account
    Then the consolidated accounts response is an empty array

  @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario: E2E.03 Non-existent master account returns not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request consolidated accounts for a non-existent defendant account
    Then the response status code is 404
    And the consolidated accounts error response matches the standard problem detail contract for status 404
    And the consolidated accounts error title contains "Defendant Account Not Found"
    And the consolidated accounts error detail contains the requested defendant account id
    And the consolidated accounts error is non-retriable

  @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario: E2E.04 User without Search and View Accounts permission is forbidden
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request consolidated accounts for defendant account 999999999
    Then the response status code is 403
    And the consolidated accounts error response matches the standard problem detail contract for status 403
    And the consolidated accounts error title contains "Forbidden"
    And the consolidated accounts error detail contains "Search and View Accounts"
    And the consolidated accounts error is non-retriable

  @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario Outline: E2E.05 Missing or invalid credentials are rejected
    When I request consolidated accounts for defendant account 999999999 <authentication_state>
    Then the response status code is 401
    And the consolidated accounts error response matches the standard problem detail contract for status 401
    And the consolidated accounts error title contains "Unauthorized"
    And the consolidated accounts error is non-retriable

    Examples:
      | authentication_state |
      | without a token      |
      | with an invalid token |

  # NOTE: Blocked until Consolidation 2 data and a cross-BU permissioned user/account
  # combination are available in the deployed functional-test environment.
  @Ignore @JIRA-STORY:PO-2333 @JIRA-EPIC:PO-2332
  Scenario: E2E.06 Any-BU Search and View Accounts permission allows consolidated account retrieval
    Given a real master defendant account has consolidated child accounts
    And I am testing as a user with Search and View Accounts permission in a different business unit
    When I request consolidated accounts for the real master defendant account
    Then the consolidated accounts response contains the expected child account data
