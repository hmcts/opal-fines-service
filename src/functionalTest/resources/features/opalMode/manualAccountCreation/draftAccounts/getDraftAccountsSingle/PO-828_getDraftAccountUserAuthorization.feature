@Opal
Feature: PO-828 Authorization for Get Draft Account

  @PO-828 @cleanUpData @JIRA-KEY:POT-222
  Scenario: Get Draft Account - No Permission
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
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
    ### This test is currently ignored as the permissions are not quite right for this test to pass.
  @JIRA-KEY:POT-223
  Scenario: Get Draft Account - No Permission in same BU
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-10@hmcts.net" user
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

  @PO-828 @cleanUpData @JIRA-KEY:POT-224
  Scenario: Get Draft Account - Permission in different BU
    Given I am testing as the "opal-test@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-8@hmcts.net" user
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
