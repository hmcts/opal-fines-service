@Opal
Feature: Update defendant accounts

  @cleanUpData
  Scenario: Updating comments and notes preserves their maximum lengths
    Given an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                           |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                         |
      | account_status    | Submitted                                    |
      | submitted_by      | DEFUPD001                                    |
      | submitted_by_name | Laura Clerk                                  |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/commentAndNotesMaxLengths.json"
    When I request at-a-glance for the created defendant account
    Then the at-a-glance response contains comments and notes from fixture "defendantAccounts/updateJson/commentAndNotesMaxLengths.json"

  @cleanUpData
  Scenario: Updating the enforcement court is reflected in enforcement status
    Given an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                           |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                         |
      | account_status    | Submitted                                    |
      | submitted_by      | DEFUPD002                                    |
      | submitted_by_name | Laura Clerk                                  |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/enforcementCourtUpdate.json"
    Then the created defendant account enforcement status contains the following data
      | enforcement_overview.enforcement_court.court_id | 770000000021 |

  @cleanUpData
  Scenario: Updating the collection-order flag is reflected in enforcement status
    Given an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                           |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                         |
      | account_status    | Submitted                                    |
      | submitted_by      | DEFUPD003                                    |
      | submitted_by_name | Laura Clerk                                  |
    When I patch the created defendant account using fixture "defendantAccounts/updateJson/collectionOrderUpdate.json"
    Then the created defendant account enforcement status contains the following data
      | enforcement_overview.collection_order.collection_order_flag | false |
