@Opal
Feature: PO-559 get draft account

  @PO-559 @cleanUpData
  Scenario: Get draft account - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                  |
      | account_type                        | Fine                |
      | account_status                      | Submitted           |
      | account_snapshot.defendant_name     | LNAME, FNAME        |
      | account_snapshot.date_of_birth      | 2000-01-01          |
      | account_snapshot.account_type       | Fine                |
      | account_snapshot.submitted_by       | BUUID               |
      | account_snapshot.business_unit_name | West London         |

    Then I delete the created draft accounts
