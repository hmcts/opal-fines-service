@Opal @JIRA-LABEL:account-enquiry @R1BDrop2
Feature: Defendant Account Remove Enforcement Hold

  @cleanUpData @JIRA-STORY:PO-1775 @JIRA-EPIC:PO-1675 @JIRA-TEST-KEY:PO-11199
  Scenario: Removing an enforcement hold preserves a maximum-length reason in account history
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source                       | MAC                                           |
      | business_unit_id             | 77                                            |
      | account                      | draftAccounts/accountJson/historyAccount.json |
      | account_enforcement_court_id | 770000000001                                  |
      | account_type                 | Fine                                          |
      | account_status               | Submitted                                     |
      | submitted_by                 | DEFHOLD11199                                  |
      | submitted_by_name            | Laura Clerk                                   |
    Then the created defendant account enforcement status contains the following data
      | last_enforcement_action.enforcement_action.result_id | NOENF |
    When I remove the created defendant account enforcement hold with the 24-character reason "Hold removal approved QA" in business unit "77"
    Then the created defendant account enforcement hold is removed
    When I request defendant account history for the created defendant account with query "itemTypes=note"
    Then the defendant account history records the submitted 24-character enforcement hold removal reason
