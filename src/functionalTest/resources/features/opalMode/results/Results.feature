@Opal @JIRA-LABEL:reference-data
Feature: Results Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-703 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5738
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

  @JIRA-STORY:PO-703 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5739
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

  @JIRA-STORY:PO-3765 @Ignore @release-1b
  Scenario: Result filtering is available when release-1b is enabled
    When I request results for identifiers "NBWT,NAP" using filter "enforcement_override" with value "true"
    Then 1 results are returned

  @JIRA-STORY:PO-3765 @Ignore @release-1b
  Scenario: Result filtering is rejected when release-1b is disabled
    When I request results using filter "active" with value "true"
    Then the response status code is 405
