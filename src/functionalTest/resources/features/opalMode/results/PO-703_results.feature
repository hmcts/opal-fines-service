@Opal
Feature: PO-703 results happy path

  @PO-703
  Scenario: get results - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to get the results ""
    Then The results response contains 59 results

    And The results response contains the following result
      | result_id                   | REM                                           |
      | result_title                | Final reminder of unpaid fine                 |
      | result_title_cy             | Nodyn atgoffa terfynol am ddirwy heb ei thalu |
      | active                      | true                                          |
      | result_type                 | Result                                        |
      | imposition_creditor         |                                               |
      | imposition_allocation_order |                                               |


    And The results response contains the following result
      | result_id                   | UPWO              |
      | result_title                | UNPAID WORK ORDER |
      | result_title_cy             |                   |
      | active                      | true              |
      | result_type                 | Result            |
      | imposition_creditor         |                   |
      | imposition_allocation_order |                   |

    And The results response contains the following result
      | result_id                   | FCOMP        |
      | result_title                | Compensation |
      | result_title_cy             | Iawndal      |
      | active                      | true         |
      | result_type                 | Result       |
      | imposition_creditor         | Any          |
      | imposition_allocation_order | 1            |

  @PO-703
  Scenario: get results - happy path filtered by result id
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to get the results "FO,ABDC"
    Then The results response contains 2 results

    And The results response contains the following result
      | result_id                   | FO     |
      | result_title                | Fine   |
      | result_title_cy             | Dirwy  |
      | active                      | true   |
      | result_type                 | Result |
      | imposition_creditor         | CF     |
      | imposition_allocation_order |        |

    And The results response contains the following result
      | result_id                   | ABDC                                    |
      | result_title                | Application made for benefit deductions |
      | result_title_cy             | Cais am dynnu arian o fudd-daliadau     |
      | active                      | true                                    |
      | result_type                 | Result                                  |
      | imposition_creditor         |                                         |
      | imposition_allocation_order |                                         |


