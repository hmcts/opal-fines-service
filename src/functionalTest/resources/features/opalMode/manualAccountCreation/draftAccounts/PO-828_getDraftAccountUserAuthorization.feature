@Opal
Feature: PO-828 Authorization for get draft account

  @PO-828 @cleanUpData
  Scenario: a user has authorization on create of manage draft account but not all the permissions
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

    Given I am testing as the "opal-test-2@hmcts.net" user
    Then I get the single created draft account and the response contains
      | business_unit_id                    |  |
      | account_type                        |  |
      | account_status                      |  |
      | account_snapshot.defendant_name     |  |
      | account_snapshot.date_of_birth      |  |
      | account_snapshot.account_type       |  |
      | account_snapshot.submitted_by       |  |
      | account_snapshot.business_unit_name |  |
    Then The draft account response returns 403
    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts

  @PO-828 @cleanUpData

  Scenario: a user has no permission to create or manage Draft Accounts/ Check or validate draft account in same BU
    Given I am testing as the "opal-test-2@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403

  @PO-828 @cleanUpData

  Scenario: a user has no permission to create or manage Draft Accounts in other BU
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

    Given I am testing as the "opal-test-3@hmcts.net" user
    Then I get the single created draft account and the response contains
      | business_unit_id                    |  |
      | account_type                        |  |
      | account_status                      |  |
      | account_snapshot.defendant_name     |  |
      | account_snapshot.date_of_birth      |  |
      | account_snapshot.account_type       |  |
      | account_snapshot.submitted_by       |  |
      | account_snapshot.business_unit_name |  |
    Then The draft account response returns 403


  Scenario: a user has no permission to create or manage account, only can check within BU
    Given I am testing as the "opal-test-4@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 47                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403









