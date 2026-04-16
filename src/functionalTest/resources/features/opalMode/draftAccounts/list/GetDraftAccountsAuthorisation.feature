@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Get Draft Accounts Authorisation

  @JIRA-STORY:PO-829 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Get Draft Accounts - No Permission
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
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
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    Then I get the draft accounts filtering on the Business unit "78"
    Then The draft account response returns 403
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

    Then I delete the created draft accounts


  @JIRA-STORY:PO-829 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Get Draft Accounts - account created in BU requesting user doesn't have permission to
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

    When I create a draft account with the following details
      | business_unit_id  | 80                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-3@dev.platform.hmcts.net" user
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

    Given I am testing as the "opal-test-10@dev.platform.hmcts.net" user

    When I get the draft accounts filtering on the Business unit ""
    Then The draft account filtered response does not contain accounts in the "73" business unit
    Then The draft account filtered response does not contain accounts in the "26" business unit
