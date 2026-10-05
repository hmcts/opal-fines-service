@Opal @JIRA-LABEL:account-enquiry @R1BDrop1 @ExcludeFromDemoNightlyAsNotLegacy
Feature: Defendant Account Enforcements

  @cleanUpData @JIRA-STORY:PO-1854 @JIRA-EPIC:PO-1675 @JIRA-TEST-KEY:PO-5620
  Scenario: An enforcement override can be applied to an enforceable defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | DEFENF001                                   |
      | submitted_by_name | Laura Clerk                                 |
    When I apply the following enforcement override to the created defendant account
      | business_unit_id               | 77           |
      | enforcement_override_result_id | FWEC         |
      | enforcer_id                    | 770000000001 |

    Then the created defendant account enforcement status contains the following data
      | enforcement_override.enforcement_override_result.enforcement_override_result_id | FWEC         |
      | enforcement_override.enforcer.enforcer_id                                       | 770000000001 |

  @cleanUpData @JIRA-STORY:PO-10811 @JIRA-STORY:PO-10826 @JIRA-EPIC:PO-978
  Scenario: A manual enforcement action can be added with hearing court and date responses
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | DEFENF002                                   |
      | submitted_by_name | Laura Clerk                                 |
    When I add the following enforcement action to the created defendant account
      | business_unit_id | 77         |
      | result_id        | NAWT       |
      | reason           | a          |
      | hearingdate      | 2026-10-02 |
      | courtcode        | 777        |
    Then the created defendant account enforcement status contains the following data
      | last_enforcement_action.enforcement_action.result_id       | NAWT       |
      | last_enforcement_action.result_responses[0].parameter_name | reason     |
      | last_enforcement_action.result_responses[0].response       | a          |
      | last_enforcement_action.result_responses[1].parameter_name | hearingdate |
      | last_enforcement_action.result_responses[1].response       | 2026-10-02 |
      | last_enforcement_action.result_responses[2].parameter_name | courtcode  |
      | last_enforcement_action.result_responses[2].response       | 777        |
    When I request defendant account history for the created defendant account with query "itemTypes=enforcement"
    Then the defendant account history contains enforcement action "NAWT" with hearing court id 770000000001 and hearing date "2026-10-02"
