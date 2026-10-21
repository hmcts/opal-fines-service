@Legacy @JIRA-LABEL:account-enquiry @R1BDrop1
Feature: Defendant Account Enforcement Overrides API In Legacy Mode

  @cleanUpData @JIRA-STORY:PO-1854 @JIRA-EPIC:PO-1675
  Scenario: Legacy enforcement override can be applied to an enforceable defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | business_unit_id             | 77                                          |
      | account                      | draftAccounts/accountJson/adultAccount.json |
      | account_type                 | Fine                                        |
      | account_status               | Submitted                                   |
      | submitted_by                 | DEFENF101                                   |
      | submitted_by_name            | Laura Clerk                                 |
      | account_enforcement_court_id | 770000000001                                |
    When I apply the following enforcement override to the created defendant account
      | business_unit_id               | 77           |
      | enforcement_override_result_id | NBWT         |
      | enforcer_id                    | 770000000001 |

    Then the created defendant account enforcement status contains the following data
      | enforcement_override.enforcement_override_result.enforcement_override_result_id | NBWT         |
      | enforcement_override.enforcer.enforcer_id                                       | 770000000001 |
      | enforcement_overview.enforcement_court.court_id                                 | 770000000001 |

  @JIRA-STORY:PO-11108 @JIRA-EPIC:PO-2063
  Scenario: Enforcement status remains available when Legacy omits the enforcement court
    # Requires pre-prod Legacy account 1300000008894 to retain the missing-court data reported in PO-11108.
    # Other environments need an equivalent Legacy fixture for this account; draft publication supplies a court.
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request legacy enforcement status for defendant account "1300000008894"
    Then the request succeeds
    And the enforcement status includes an explicitly null enforcement court
    And the enforcement status response contains the following data
      | enforcement_overview.days_in_default                                              | 0                  |
      | enforcement_overview.collection_order.collection_order_flag                       | false              |
      | enforcement_override.enforcement_override_result.enforcement_override_result_id   | MAN                |
      | enforcement_override.enforcement_override_result.enforcement_override_result_name | MANUAL ENFORCEMENT |
      | last_enforcement_action.enforcement_action.result_id                              | NOENF              |
      | last_enforcement_action.enforcement_action.result_title                           | ENFORCEMENT HELD   |
