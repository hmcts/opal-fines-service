@Opal
Feature: PO-746 put/update draft account

  @PO-746 @cleanUpData
  Scenario: Update draft account - update account details
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    |                                        |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     |                                        |
    Then The draft account response returns 201
    And I store the created draft account ID
    And I store the created draft account created_at time

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       |             |
      | account_snapshot.submitted_by       | BUUID       |
      | account_snapshot.business_unit_name | West London |

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     |                                             |
    Then The draft account response returns 200


    And I get the single created draft account and the response contains
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Resubmitted  |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 01/01/2000   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | BUUID        |
      | account_snapshot.business_unit_name | West London  |

    Then I see the created at time hasn't changed

    Then I delete the created draft accounts
