@Legacy
Feature: Update defendant accounts in Legacy mode
  @cleanUpData @JIRA-STORY:PO-11193 @JIRA-EPIC:PO-8248 @R1BDrop2
  Scenario: Legacy comments and notes preserve their maximum lengths
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id             | 77                                           |
      | account                      | draftAccounts/accountJson/adultAccount.json |
      | account_type                 | Fine                                         |
      | account_status               | Submitted                                    |
      | account_enforcement_court_id | 770000000001                                 |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/commentAndNotesMaxLengths.json"
    When I request at-a-glance for the created defendant account
    Then the at-a-glance response contains comments and notes from fixture "defendantAccounts/updateJson/commentAndNotesMaxLengths.json"

  @cleanUpData @JIRA-STORY:PO-11193 @JIRA-EPIC:PO-8248 @R1BDrop2
  Scenario: Legacy enforcement court update is reflected in enforcement status
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id             | 77                                           |
      | account                      | draftAccounts/accountJson/adultAccount.json |
      | account_type                 | Fine                                         |
      | account_status               | Submitted                                    |
      | account_enforcement_court_id | 770000000001                                 |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/enforcementCourtUpdate.json"
    Then the created defendant account enforcement status contains the following data
      | enforcement_overview.enforcement_court.court_id | 770000000021 |

  @cleanUpData @JIRA-STORY:PO-11193 @JIRA-EPIC:PO-8248 @R1BDrop2
  Scenario: Legacy collection-order update is reflected in enforcement status
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id             | 77                                           |
      | account                      | draftAccounts/accountJson/adultAccount.json |
      | account_type                 | Fine                                         |
      | account_status               | Submitted                                    |
      | account_enforcement_court_id | 770000000001                                 |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/collectionOrderUpdate.json"
    Then the created defendant account enforcement status contains the following data
      | enforcement_overview.collection_order.collection_order_flag | false |
