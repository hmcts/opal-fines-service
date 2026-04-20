@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Enforcements

  @cleanUpData @JIRA-STORY:PO-1854 @JIRA-EPIC:PO-1675 @JIRA-KEY:POT-6017
  Scenario: Enforcement override happy path
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | DEFENF001                                   |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I am testing as the "opal-test-10@dev.platform.hmcts.net" user
    And I patch the draft account with the following details
      | business_unit_id | 77                 |
      | account_status   | Publishing Pending |
      | validated_by     | DEFENF001_REVIEWER |
      | If-Match         | 0                  |
    Then The draft account response returns 200
    And I store the created defendant account ID from the draft account response

    When I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I get the created defendant account enforcement status
    Then The defendant account enforcement response returns 200

    When I patch the created defendant account enforcement override with the following details
      | business_unit_id               | 77           |
      | enforcement_override_result_id | FWEC         |
      | enforcer_id                    | 770000000001 |
    Then The defendant account enforcement response returns 200

    When I get the created defendant account enforcement status
    Then The defendant account enforcement response returns 200
    And The defendant account enforcement response contains
      | enforcement_override.enforcement_override_result.enforcement_override_result_id | FWEC         |
      | enforcement_override.enforcer.enforcer_id                                       | 770000000001 |

    Then I delete the created defendant accounts
    Then I delete the created draft accounts
