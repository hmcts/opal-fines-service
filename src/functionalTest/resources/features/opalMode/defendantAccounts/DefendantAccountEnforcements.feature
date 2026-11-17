@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Enforcements

  @R1BDrop1 @cleanUpData @JIRA-STORY:PO-1854 @JIRA-EPIC:PO-1675 @JIRA-TEST-KEY:PO-5620
  Scenario: An enforcement override can be applied to an enforceable defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source            | MAC                                         |
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

  @R1BDrop1 @cleanUpData @JIRA-STORY:PO-10811 @JIRA-STORY:PO-10826 @JIRA-EPIC:PO-978
  Scenario: A manual enforcement action can be added with hearing court and date responses
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source            | MAC                                         |
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

  # These examples cover the actions supported by Capita LLD section 3.19.1.
  # CW uses business unit 78 because it has the prison details needed for the test.
  # Each text value uses the maximum length allowed for the selected action.
  # After adding the action, we check that the values returned match those submitted.
  # Some values are not returned by Legacy, so we can only check that they were accepted.
  # The notes beside each example explain these limits.
  # Dates, numbers, courts and enforcers use suitable test values for each system
  @R1BDrop2 @JIRA-EPIC:PO-978 @cleanUpData @JIRA-STORY:PO-11198
  Scenario Outline: Maximum-length enforcement text is accepted and exposed values are preserved for <result>
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source                       | MAC                                               |
      | business_unit_id             | <business_unit>                                   |
      | account_enforcement_court_id | <court_id>                                        |
      | account                      | draftAccounts/accountJson/enforcementAccount.json |
      | account_type                 | Fine                                              |
      | account_status               | Submitted                                         |
      | submitted_by                 | MAXENF001                                         |
      | submitted_by_name            | Laura Clerk                                       |
    And a maximum-length enforcement fixture for "<result>" in "English"
    When I submit the enforcement parameter fixture for business unit "<business_unit>"
    Then the exposed enforcement parameters match the submitted content and lengths

    Examples:
      | result | business_unit | court_id     | notes |
      | ABDC   | 77            | 770000000001 |  |
      | AEO    | 77            | 770000000001 |  |
      | AEOC   | 77            | 770000000001 |  |
      | BWTU   | 77            | 770000000001 | Legacy accepts baildirection and courtdetails but does not return them for checking. |
      | CLAMPO | 77            | 770000000001 |  |
      | COLLO  | 77            | 770000000001 | Legacy accepts reserveterms but does not return it for checking. |
      | CONF   | 77            | 770000000001 |  |
      | CW     | 78            | 780000000001 | Legacy accepts detailsconsecutive, processserver, basisofcommital and reasonnoaltused but does not return them for checking. |
      | DW     | 77            | 770000000001 |  |
      | FSN    | 77            | 770000000001 |  |
      | HTT    | 77            | 770000000001 |  |
      | INTL   | 77            | 770000000001 |  |
      | MPSO   | 77            | 770000000001 | Legacy accepts detailsifother but does not return it for checking. |
      | NAP    | 77            | 770000000001 |  |
      | NBWT   | 77            | 770000000001 | Legacy accepts courtdetails but does not return it for checking. |
      | NOENF  | 77            | 770000000001 |  |
      | PRIS   | 77            | 770000000001 | Legacy accepts prisonandprisonnumber but does not return it for checking. The release date is checked in account history. |
      | REGF   | 77            | 770000000001 |  |
      | REM    | 77            | 770000000001 |  |
      | S136   | 77            | 770000000001 | Legacy accepts timeofrelease but does not return it for checking. |
      | SC     | 77            | 770000000001 | Legacy accepts paymentterms but does not return it for checking. |
      | UPWO   | 77            | 770000000001 | Legacy accepts detailsifother and supervisingcourt but does not return them for checking. |
      | WDN    | 77            | 770000000001 |  |

  # cy_ parameters are supplied by GET /results/{id}?include_welsh=true.
  # Notes identify text fields absent from Legacy enforcement status; their persisted content is not verified.
  @R1BDrop2 @JIRA-EPIC:PO-978 @cleanUpData @JIRA-STORY:PO-11198
  Scenario Outline: Maximum-length Welsh text is preserved for enforcement result <result>
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source            | MAC                                               |
      | business_unit_id  | 77                                                |
      | account           | draftAccounts/accountJson/enforcementAccount.json |
      | account_type      | Fine                                              |
      | account_status    | Submitted                                         |
      | submitted_by      | MAXENF001                                         |
      | submitted_by_name | Laura Clerk                                       |
    And a maximum-length enforcement fixture for "<result>" in "Welsh"
    When I submit the enforcement parameter fixture for business unit "77"
    Then the exposed enforcement parameters match the submitted content and lengths

    Examples:
      | result | notes |
      | COLLO  | Legacy accepts reserveterms and cy_reserveterms but does not return them for checking. |
      | SC     | Legacy accepts paymentterms and cy_paymentterms but does not return them for checking. |
      | UPWO   | Legacy accepts detailsifother, supervisingcourt and cy_supervisingcourt but does not return them for checking. |

  @R1BDrop2 @JIRA-EPIC:PO-978 @cleanUpData @JIRA-STORY:PO-11198
  @JIRA-DEFECT:PO-11610
  Scenario Outline: Maximum-length enforcement text and optional <terms> payment terms are preserved for <result>
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source            | MAC                                               |
      | business_unit_id  | 77                                                |
      | account           | draftAccounts/accountJson/enforcementAccount.json |
      | account_type      | Fine                                              |
      | account_status    | Submitted                                         |
      | submitted_by      | MAXENF001                                         |
      | submitted_by_name | Laura Clerk                                       |
    And a maximum-length enforcement fixture for "<result>" in "English"
    And the enforcement fixture includes "<terms>" payment terms
    When I submit the enforcement parameter fixture for business unit "77"
    Then the payment terms GET preserves the submitted enforcement payment terms
    And the exposed enforcement parameters match the submitted content and lengths

    Examples:
      | result | terms       | notes |
      | COLLO  | By date     | Legacy accepts reserveterms but does not return it for checking. |
      | COLLO  | Instalments | Legacy accepts reserveterms but does not return it for checking. |
