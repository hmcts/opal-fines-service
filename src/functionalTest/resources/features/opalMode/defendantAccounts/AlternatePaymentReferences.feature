@Opal @JIRA-LABEL:apr-management @JIRA-STORY:PO-10905 @JIRA-EPIC:PO-7465
Feature: Alternate payment reference management

  @R1COff @JIRA-STORY:PO-10877 @ignore
  Scenario: 01 - GET is unavailable when alternate payment references are disabled
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I retrieve alternate payment references for the created defendant account
    Then the response status code is 404

  @JIRA-STORY:PO-10877
  @JIRA-STORY:PO-10881
  Scenario: 02 - GET is forbidden without Manage Alternate Payment References permission
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I retrieve alternate payment references for the created defendant account as "opal-test-2@dev.platform.hmcts.net"
    Then the error response matches the following values
      | field  | expected                                           |
      | status | 403                                                |
      | title  | Forbidden                                          |
      | detail | You do not have permission to access this resource |
      | type   | https://hmcts.gov.uk/problems/forbidden            |

  @JIRA-STORY:PO-10877
  Scenario: 03 - GET returns the defendant account's alternate payment reference
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    And an alternate payment reference exists for the created defendant account
    When I retrieve alternate payment references for the created defendant account
    Then the response status code is 200
    And the alternate payment references response is as expected

  @JIRA-STORY:PO-10877
  Scenario: 04 - GET returns an empty list when the defendant has no alternate payment reference
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I retrieve alternate payment references for the created defendant account
    Then the response status code is 200
    And the alternate payment references response is an empty array

  @JIRA-STORY:PO-10877
  Scenario: 05 - GET returns not found for a non-existent defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I retrieve alternate payment references for a non-existent defendant account
    Then the error response matches the following values
      | field  | expected                                       |
      | status | 404                                            |
      | title  | Entity Not Found                               |
      | detail | The requested entity could not be found        |
      | type   | https://hmcts.gov.uk/problems/entity-not-found |

  @JIRA-STORY:PO-10878
  Scenario: 06 - POST creates an alternate payment reference
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I create an alternate payment reference for the created defendant account
    Then the response status code is 201
    And the POST response contains the expected alternate payment reference

  @JIRA-STORY:PO-10878
  Scenario: 07 - POST rejects a duplicate alternate payment reference within a business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    And a defendant account "Account2" exists in business unit "77"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    When I create an alternate payment reference with the same name on defendant account "Account2"
    Then the error response matches the following values
      | field      | expected                                                                                |
      | status     | 422                                                                                     |
      | title      | Alternate Payment Reference text already exists in business unit                        |
      | detail     | Alternate Payment Reference text: ${createdAprText} already exists in business unit: 77 |
      | type       | https://hmcts.gov.uk/problems/BusinessRuleViolationException                            |
      | is_warning | true                                                                                    |
    And defendant account "Account2" has no alternate payment reference

  @JIRA-STORY:PO-10878
  Scenario: 08 - POST rejects a second alternate payment reference for the same defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    And the POST response contains the expected alternate payment reference
    When I create another alternate payment reference for defendant account "Account1"
    Then the error response matches the following values
      | field      | expected                                                                                                             |
      | status     | 422                                                                                                                  |
      | title      | Alternate Payment Reference already exists for defendant account                                                     |
      | detail     | Alternate Payment Reference with relationship APR already exists for defendant account: ${createdDefendantAccountId} |
      | type       | https://hmcts.gov.uk/problems/BusinessRuleViolationException                                                         |
      | is_warning | true                                                                                                                 |
    And defendant account "Account1" still has its existing alternate payment reference

  @JIRA-STORY:PO-10878
  Scenario: 09 - POST allows the same alternate payment reference in different business units
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    And a defendant account "Account2" exists in business unit "78"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    When I create the same alternate payment reference on defendant account "Account2"
    Then the response status code is 201
    And the POST response contains the expected alternate payment reference

  @JIRA-STORY:PO-10878
  Scenario: 10 - POST rejects an alternate payment reference with invalid length
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    When I create an alternate payment reference with invalid length
    Then the error response matches the following values
      | field  | expected                                                 |
      | status | 400                                                      |
      | title  | Bad Request                                              |
      | detail | The request does not conform to the required JSON schema |
      | type   | https://hmcts.gov.uk/problems/json-schema-validation     |
    And defendant account "Account1" has no alternate payment reference

  @JIRA-STORY:PO-10879
  Scenario: 11 - PATCH updates an alternate payment reference
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I create an alternate payment reference for the created defendant account
    Then the response status code is 201
    When I update the created alternate payment reference
    Then the response status code is 200
    And the PATCH response contains the expected alternate payment reference

  @JIRA-STORY:PO-10879
  Scenario: 12 - PATCH rejects an update with an outdated version
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    When I create an alternate payment reference for the created defendant account
    Then the response status code is 201
    When I update the created alternate payment reference with an outdated version
    Then the error response matches the following values
      | field        | expected                                                                                                                                                                  |
      | status       | 409                                                                                                                                                                       |
      | title        | Conflict                                                                                                                                                                  |
      | detail       | :updateAlternatePaymentReferences: Versions do not match for: DefendantAccountEntity '${createdDefendantAccountId}'; DB version: 2, supplied update version: 999999999999 |
      | type         | https://hmcts.gov.uk/problems/optimistic-locking                                                                                                                          |
      | resourceType | uk.gov.hmcts.opal.entity.draft.DraftAccountEntity                                                                                                                         |
      | resourceId   | ${createdDefendantAccountId}                                                                                                                                              |
      | retriable    | false                                                                                                                                                                     |
    And defendant account "Account1" still has its existing alternate payment reference

  @JIRA-STORY:PO-10879
  Scenario: 13 - PATCH rejects a duplicate name in the same business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    And a defendant account "Account2" exists in business unit "77"
    And an alternate payment reference exists for defendant account "Account1"
    And an alternate payment reference exists for defendant account "Account2"
    When I update defendant account "Account1" APR to the name on defendant account "Account2"
    Then the error response matches the following values
      | field        | expected                                                                                  |
      | status       | 422                                                                                       |
      | title        | Alternate Payment Reference text already exists in business unit                          |
      | detail       | Alternate Payment Reference text: ${aprText_Account2} already exists in business unit: 77 |
      | type         | https://hmcts.gov.uk/problems/BusinessRuleViolationException                              |
      | is_warning   | true                                                                                      |
      | is_retryable | false                                                                                     |
    And defendant account "Account1" still has its existing alternate payment reference

  @JIRA-STORY:PO-10879
  Scenario: 14 - PATCH allows the same name in a different business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    And a defendant account "Account2" exists in business unit "78"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    And I create a different alternate payment reference on defendant account "Account2"
    Then the response status code is 201
    When I update defendant account "Account1" APR to the name on defendant account "Account2"
    Then the response status code is 200
    And the PATCH response contains the expected alternate payment reference

  @JIRA-STORY:PO-10879
  Scenario: 15 - PATCH rejects an alternate payment reference with invalid length
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    When I update the created alternate payment reference with invalid length
    Then the error response matches the following values
      | field  | expected                                                 |
      | status | 400                                                      |
      | title  | Bad Request                                              |
      | detail | The request does not conform to the required JSON schema |
      | type   | https://hmcts.gov.uk/problems/json-schema-validation     |
    And defendant account "Account1" still has its existing alternate payment reference

  @JIRA-STORY:PO-10880
  Scenario: 16 - DELETE removes alternate payment references from supported account types
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account exists in business unit "77"
    When I create an alternate payment reference for the created defendant account
    Then the response status code is 201
    When I delete the created alternate payment reference
    Then the response status code is 200
    When I retrieve alternate payment references for the created defendant account
    Then the response status code is 200
    And the alternate payment references response is an empty array

  @JIRA-STORY:PO-10880
  Scenario: 17 - DELETE rejects a request with an outdated version
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a defendant account "Account1" exists in business unit "77"
    When I create an alternate payment reference for defendant account "Account1"
    Then the response status code is 201
    When I delete the created alternate payment reference with an outdated version
    Then the error response matches the following values
      | field        | expected                                                                                                                                                                  |
      | status       | 409                                                                                                                                                                       |
      | title        | Conflict                                                                                                                                                                  |
      | detail       | :deleteAlternatePaymentReferences: Versions do not match for: DefendantAccountEntity '${createdDefendantAccountId}'; DB version: 2, supplied update version: 999999999999 |
      | type         | https://hmcts.gov.uk/problems/optimistic-locking                                                                                                                          |
      | resourceType | uk.gov.hmcts.opal.entity.draft.DraftAccountEntity                                                                                                                         |
      | resourceId   | ${createdDefendantAccountId}                                                                                                                                              |
      | retriable    | false                                                                                                                                                                     |
    And defendant account "Account1" still has its existing alternate payment reference
