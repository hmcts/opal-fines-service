@Opal @JIRA-LABEL:reference-data
Feature: Results Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-703 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5738 @R1CReferenceData
  Scenario: All results are returned when no result id filter is supplied
    When I request results for identifiers ""
    Then 60 results are returned

    And the returned results include the following result
      | result_id                   | REM                                           |
      | result_title                | Reminder of Unpaid Fine                       |
      | result_title_cy             | Nodyn atgoffa terfynol am ddirwy heb ei thalu |
      | active                      | true                                          |
      | result_type                 | Result                                        |
      | imposition_creditor         |                                               |
      | imposition_allocation_order |                                               |


    And the returned results include the following result
      | result_id                   | UPWO              |
      | result_title                | Unpaid Work Order |
      | result_title_cy             |                   |
      | active                      | true              |
      | result_type                 | Result            |
      | imposition_creditor         |                   |
      | imposition_allocation_order |                   |

    And the returned results include the following result
      | result_id                   | FCOMP        |
      | result_title                | Compensation |
      | result_title_cy             | Iawndal      |
      | active                      | true         |
      | result_type                 | Result       |
      | imposition_creditor         | Any          |
      | imposition_allocation_order | 1            |

  @JIRA-STORY:PO-703 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5739 @R1CReferenceData
  Scenario: Only requested results are returned when result ids are supplied
    When I request results for identifiers "FO,ABDC"
    Then 2 results are returned

    And the returned results include the following result
      | result_id                   | FO     |
      | result_title                | Fine   |
      | result_title_cy             | Dirwy  |
      | active                      | true   |
      | result_type                 | Result |
      | imposition_creditor         | CF     |
      | imposition_allocation_order | 6      |

    And the returned results include the following result
      | result_id                   | ABDC                                    |
      | result_title                | Application made for Benefit Deductions |
      | result_title_cy             | Cais am dynnu arian o fudd-daliadau     |
      | active                      | true                                    |
      | result_type                 | Result                                  |
      | imposition_creditor         |                                         |
      | imposition_allocation_order |                                         |

  @JIRA-STORY:PO-6425 @JIRA-EPIC:PO-1674 @JIRA-TEST-KEY:PO-7869 @R1B
  Scenario: Result by ID includes employment data requirement flag
    When I request result with identifier "AEO"
    Then the result response contains
      | result_id                | AEO  |
      | requires_employment_data | true |

  @JIRA-STORY:PO-8973 @JIRA-EPIC:PO-304 @R1B @JIRA-TEST-KEY:PO-10065
  Scenario: Result by ID matches OpenAPI schema
    When I request result with identifier "SC"
    Then the result response matches the documented schema

  @JIRA-STORY:PO-8973 @JIRA-EPIC:PO-304 @R1B @JIRA-TEST-KEY:PO-10066
  Scenario: Result by ID maps response fields
    When I request result with identifier "SC"
    Then the result response contains
      | result_id                     | SC                                                            |
      | result_title                  | Suspended imprisonment to enforce money owed                  |
      | result_title_cy               | Carchar Gohiriedig i orfodi arian sy'n ddyledus               |
      | result_type                   | Result                                                        |
      | active                        | true                                                          |
      | imposition                    | false                                                         |
      | imposition_accruing           | false                                                         |
      | enforcement                   | true                                                          |
      | enforcement_override          | false                                                         |
      | further_enforcement_warn      | false                                                         |
      | further_enforcement_disallow  | false                                                         |
      | enforcement_hold              | false                                                         |
      | requires_enforcer             | false                                                         |
      | generates_hearing             | false                                                         |
      | collection_order              | false                                                         |
      | extend_ttp_disallow           | true                                                          |
      | extend_ttp_preserve_last_enf  | false                                                         |
      | prevent_payment_card          | false                                                         |
      | lists_monies                  | true                                                          |
      | requires_employment_data      | false                                                         |
      | allow_payment_terms           | false                                                         |
      | allow_additional_action       | false                                                         |
      | generates_warrant             | false                                                         |
      | requires_lja                  | false                                                         |
      | manual_enforcement            | true                                                          |
      | enf_next_permitted_actions    | CWN                                                           |

  @JIRA-STORY:PO-8973 @JIRA-EPIC:PO-304 @R1B @JIRA-TEST-KEY:PO-10067
  Scenario: Unknown result by ID returns not found
    When I request result with identifier "ZZZZZZ"
    Then the request is rejected as not found

  @JIRA-STORY:PO-8973 @JIRA-EPIC:PO-2630 @R1B @JIRA-TEST-KEY:PO-10068
  Scenario: Result by ID omits Welsh parameters by default
    When I request result with identifier "SC"
    Then the result parameters do not contain the following entries
      | cy_paymentterms |

  @JIRA-STORY:PO-2985 @JIRA-EPIC:PO-2630 @R1B @JIRA-TEST-KEY:PO-9560
  Scenario: Result by ID can include Welsh text result parameters
    When I request result with identifier "SC" including Welsh parameters
    Then the result parameters contain the following entries in order
      | name            | type | language_dependent | hint                                          |
      | paymentterms    | text | true               |                                               |
      | cy_paymentterms | text | true               | Provide a welsh version for the defendant     |

  @JIRA-STORY:PO-9108 @JIRA-EPIC:PO-2630 @R1B @JIRA-TEST-KEY:PO-9561
  Scenario: Result by ID can include Welsh date result parameters
    When I request result with identifier "CLAMPO" including Welsh parameters
    Then the result parameters contain the following entries in order
      | name             | type | language_dependent | hint                                          |
      | effectivedate    | date | true               |                                               |
      | cy_effectivedate | date | true               | Provide a welsh version for the defendant    |

  @JIRA-STORY:PO-3765 @Ignore @R1B
  Scenario: Result filtering is available when release-1b is enabled
    When I request results for identifiers "NBWT,NAP" using filter "enforcement_override" with value "true"
    Then 1 results are returned

  @JIRA-STORY:PO-3765 @Ignore @R1B
  Scenario: Result filtering is rejected when release-1b is disabled
    When I request results using filter "active" with value "true"
    Then the response status code is 404
