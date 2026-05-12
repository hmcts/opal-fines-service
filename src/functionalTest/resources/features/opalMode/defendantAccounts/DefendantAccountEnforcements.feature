@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Enforcement Overrides

  @cleanUpData @JIRA-STORY:PO-1854 @JIRA-EPIC:PO-1675
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
