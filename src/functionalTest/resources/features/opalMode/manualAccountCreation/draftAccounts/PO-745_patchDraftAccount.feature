@Opal
Feature: PO-745 patch draft account

  @PO-745 @cleanUpData
  Scenario: Patch draft account - Pending - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73             |
      | account_status   | Pending        |
      | validated_by     | BUUID_REVIEWER |
    Then The draft account response returns 200

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73             |
      | account_type                        | Fine           |
      | account_status                      | Pending        |
      | account_snapshot.defendant_name     | LNAME, FNAME   |
      | account_snapshot.date_of_birth      | 01/01/2000     |
      | account_snapshot.account_type       | Fine           |
      | account_snapshot.submitted_by       | BUUID          |
      | account_snapshot.business_unit_name | West London    |
      | timeline_data.status                | Pending        |
      | timeline_data.username              | BUUID_REVIEWER |

    Then I delete the created draft accounts

  @PO-745 @cleanUpData
  Scenario: Patch draft account - Rejected - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
    Then The draft account response returns 200

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Rejected             |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 01/01/2000           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | BUUID                |
      | account_snapshot.business_unit_name | West London          |
      | timeline_data.status                | Rejected             |
      | timeline_data.username              | BUUID_REVIEWER       |
      | timeline_data.reason_text           | Reason for rejection |

    Then I delete the created draft accounts

  @PO-745 @cleanUpData
  Scenario: Patch draft account - Deleted - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                  |
      | account_status   | Deleted             |
      | validated_by     | BUUID_REVIEWER      |
      | reason_text      | Reason for deletion |
    Then The draft account response returns 200

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                  |
      | account_type                        | Fine                |
      | account_status                      | Deleted             |
      | account_snapshot.defendant_name     | LNAME, FNAME        |
      | account_snapshot.date_of_birth      | 01/01/2000          |
      | account_snapshot.account_type       | Fine                |
      | account_snapshot.submitted_by       | BUUID               |
      | account_snapshot.business_unit_name | West London         |
      | timeline_data.status                | Deleted             |
      | timeline_data.username              | BUUID_REVIEWER      |
      | timeline_data.reason_text           | Reason for deletion |

    Then I delete the created draft accounts