@Opal @JIRA-LABEL:manual-account-creation
Feature: Draft Account Snapshot Identity

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-936 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5634
  Scenario: Submitted-by snapshot values come from the access token identity
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk1                                |
    Then the draft account is created successfully with the following data
      | business_unit_id                    | 73                               |
      | account_type                        | Fine                             |
      | account_status                      | Submitted                        |
      | account_snapshot.defendant_name     | LNAME, FNAME                     |
      | account_snapshot.date_of_birth      | 2000-01-01                       |
      | account_snapshot.account_type       | Fine                             |
      | account_snapshot.submitted_by       | L073JG                           |
      | account_snapshot.submitted_by_name  | opal-test                        |
      | account_snapshot.business_unit_name | West London                      |
