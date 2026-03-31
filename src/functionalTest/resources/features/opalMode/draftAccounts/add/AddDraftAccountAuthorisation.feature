@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Add Draft Account Authorisation

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - Invalid Auth
    Given I set an invalid token
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 401

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - No Permission
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 403

  @JIRA-STORY:PO-827 @JIRA-EPIC:PO-2219 @cleanUpData
  Scenario: Post Draft Account - Permission in different BU
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 403
