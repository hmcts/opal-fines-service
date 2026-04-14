@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Get Draft Account Authorisation

  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4481
  Scenario: Get Draft Account - No Permission
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
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

    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
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
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

    Then I delete the created draft accounts

  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData
    ### This test is currently ignored as the permissions are not quite right for this test to pass.
  @JIRA-KEY:POT-4482
  Scenario: Get Draft Account - No Permission in same BU
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
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

    Given I am testing as the "opal-test-10@dev.platform.hmcts.net" user
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

  @JIRA-STORY:PO-828 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-4483
  Scenario: Get Draft Account - Permission in different BU
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
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

    Given I am testing as the "opal-test-8@dev.platform.hmcts.net" user
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

    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    Then I delete the created draft accounts
