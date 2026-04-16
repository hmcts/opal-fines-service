@Opal @JIRA-LABEL:manual-account-creation
Feature: Add Draft Account Submitted By Name

  @JIRA-STORY:PO-936 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post draft account - Submitted By Name populates snapshot
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk1                                |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 73                               |
      | account_type                        | Fine                             |
      | account_status                      | Submitted                        |
      | account_snapshot.defendant_name     | LNAME, FNAME                     |
      | account_snapshot.date_of_birth      | 2000-01-01                       |
      | account_snapshot.account_type       | Fine                             |
      | account_snapshot.submitted_by       | L073JG                           |
      | account_snapshot.submitted_by_name  | opal-test@dev.platform.hmcts.net |
      | account_snapshot.business_unit_name | West London                      |

    Then I delete the created draft accounts
