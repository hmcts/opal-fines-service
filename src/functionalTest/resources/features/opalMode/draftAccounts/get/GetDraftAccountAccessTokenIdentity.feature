@Opal @JIRA-LABEL:manual-account-creation @R1A
Feature: Draft Account Access Token Identity

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-2292 @JIRA-EPIC:PO-2808 @cleanUpData @JIRA-TEST-KEY:PO-5641
  Scenario: Access token identity populates submitted-by values
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
    Then the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Submitted    |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |
    And the request succeeds

    When I patch the draft account with the following details
      | business_unit_id | 73                  |
      | account_status   | Rejected            |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                   |
    Then the created draft account is patched successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73                  |
      | account_type                        | Fine                |
      | account_status                      | Rejected            |
      | account_snapshot.defendant_name     | LNAME, FNAME        |
      | account_snapshot.date_of_birth      | 2000-01-01          |
      | account_snapshot.account_type       | Fine                |
      | account_snapshot.submitted_by       | L073JG              |
      | account_snapshot.business_unit_name | West London         |
      | timeline_data[0].status             | Created             |
      | timeline_data[0].username           | L073JG              |
      | timeline_data[1].status             | Rejected            |
      | timeline_data[1].username           | L073JG              |
      | timeline_data[1].reason_text        | Reason for rejection |
