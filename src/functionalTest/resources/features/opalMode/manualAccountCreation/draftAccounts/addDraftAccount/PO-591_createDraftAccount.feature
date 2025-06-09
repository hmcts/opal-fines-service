@Opal
Feature: PO-591 create draft account

  @PO-591 @cleanUpData
  Scenario: Create draft account - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fines                                       |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 73                  |
      | account_type                        | Fines               |
      | account_status                      | Submitted           |
      | account_snapshot.defendant_name     | LNAME, FNAME        |
      | account_snapshot.date_of_birth      | 01/01/2000          |
      | account_snapshot.account_type       | Fine                |
      | account_snapshot.submitted_by       | BUUID               |
      | account_snapshot.submitted_by_name  | Laura Clerk         |
      | account_snapshot.business_unit_name | West London         |

    Then I delete the created draft accounts
