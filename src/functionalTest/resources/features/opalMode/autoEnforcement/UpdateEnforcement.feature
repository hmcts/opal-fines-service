@Opal @JIRA-LABEL:auto-enforcement-config
Feature: Update Auto Enforcement Config

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And the original values are remembered for the following enforcement account types
      | enforcement_account_type |
      | COLH                     |
      | COLL                     |
      | AH                       |
      | AL                       |
      | COH                      |
      | COL                      |
      | YH                       |
      | YL                       |

  @JIRA-STORY:PO-2435 @JIRA-EPIC:PO-2433 @EnforcementDataRevert
  Scenario: E2E.01 – Update multiple enforcement account types
    When I update the following enforcement account type minimum balances
      | enforcement_account_type | minimum_balance |
      | COLH                     | 200             |
      | COLL                     | 100             |
      | AH                       | 300             |
      | AL                       | 200             |
      | COH                      | 400             |
      | COL                      | 300             |
      | YH                       | 500             |
      | YL                       | 400             |
    Then the enforcement account type minimum balances should be updated with the following
      | enforcement_account_type | minimum_balance |
      | COLH                     | 200             |
      | COLL                     | 100             |
      | AH                       | 300             |
      | AL                       | 200             |
      | COH                      | 400             |
      | COL                      | 300             |
      | YH                       | 500             |
      | YL                       | 400             |

  @JIRA-STORY:PO-2435 @JIRA-EPIC:PO-2433
  Scenario: E2E.02 – Reject a null minimum balance update for a low-path enforcement account type
    When I update enforcement account type "COLL" to have a null minimum balance
    Then the request is rejected with status 422
    And enforcement account type "COLL" should remain unchanged

  @JIRA-STORY:PO-2435 @JIRA-EPIC:PO-2433
  Scenario: E2E.03 – Reject an enforcement account type update with a mismatched version
    When I update enforcement account type "AH" using an outdated version
    Then the request is rejected with status 409
    And enforcement account type "COLL" should remain unchanged

  @JIRA-STORY:PO-2435 @JIRA-EPIC:PO-2433
  Scenario: E2E.04 – Reject an update for an enforcement account type that does not exist
    When I update an enforcement account type that does not exist
    Then the request is rejected with status 404

  @JIRA-STORY:PO-2435 @JIRA-EPIC:PO-2433
  Scenario: E2E.04 – Reject an enforcement account type update without the required permission
    When I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    And I update the following enforcement account type minimum balances
      | enforcement_account_type | minimum_balance |
      | COH                      | 200             |
    Then the request is rejected as forbidden
    And enforcement account type "COH" should remain unchanged
