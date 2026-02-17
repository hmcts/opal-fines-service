@Opal
Feature: Access token identity is used and ignores supplied submitted_by and validated_by values.

  @PO-2292 @cleanUpData
  Scenario: Get Draft Accounts - Fields are populated using the access token ignore request that includes submitted_by or validated_by
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | L071JG                                       |
      | submitted_by_name | opal-test                              |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Submitted            |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |
    Then The draft account response returns 200


    When I patch the draft account with the following details
      | business_unit_id | 73                  |
      | account_status   | Deleted             |
      | validated_by     | L072JG              |
      | reason_text      | Reason for deletion |
      | If-Match         | 0                   |
    Then The draft account response returns 200

    Then I delete the created draft accounts
