@Opal
Feature: PO-829 Authorization for Get Draft Accounts

  @PO-829 @cleanUpData @JIRA-KEY:POT-214
  Scenario: Get Draft Accounts - No Permission
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 78                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    #the test user2 doesn't have permission to business unit id 78
    Given I am testing as the "opal-test-2@hmcts.net" user
    Then I get the draft accounts filtering on the Business unit "78"
    Then The draft account response returns 403
    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts


  @PO-829 @cleanUpData @JIRA-KEY:POT-215
  Scenario: Get Draft Accounts - account created in BU requesting user doesn't have permission to
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

    When I create a draft account with the following details
      | business_unit_id | 80                                           |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-3@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-10@hmcts.net" user

    When I get the draft accounts filtering on the Business unit ""
    Then The draft account filtered response does not contain accounts in the "73" business unit
    Then The draft account filtered response does not contain accounts in the "26" business unit
