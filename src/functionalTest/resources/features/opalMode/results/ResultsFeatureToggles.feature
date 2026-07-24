@Opal @JIRA-LABEL:reference-data
Feature: Results Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1AOff @JIRA-STORY:PO-3765 @JIRA-STORY:PO-3754 @JIRA-EPIC:PO-3685 @JIRA-TEST-KEY:PO-8855
  Scenario: Results endpoint is unavailable when release 1a is disabled
    When I request results for identifiers "FO,ABDC"
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

  @R1BOff @JIRA-STORY:PO-3765 @JIRA-EPIC:PO-3685 @JIRA-TEST-KEY:PO-8391
  Scenario: Results remain available without filters when release 1b is disabled
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

  @R1BOff @JIRA-STORY:PO-3765 @JIRA-EPIC:PO-3685 @JIRA-TEST-KEY:PO-8392
  Scenario: Results filters are unavailable when release 1b is disabled
    When I request results for identifiers "FO,FCOMP,ABDC" with the following filters
      | active                  | true  |
      | manual_enforcement_only | true  |
      | generates_hearing       | false |
      | enforcement             | true  |
      | enforcement_override    | true  |
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

  @R1B @JIRA-STORY:PO-3765 @JIRA-EPIC:PO-3685
  Scenario: Results filters are available when release 1b is enabled
    When I request results for identifiers "FO,FCOMP,ABDC" with the following filters
      | active                  | true  |
      | manual_enforcement_only | true  |
      | generates_hearing       | false |
      | enforcement             | true  |
      | enforcement_override    | true  |
    Then 1 results are returned

    And the returned results include the following result
      | result_id                   | ABDC                                    |
      | result_title                | Application made for Benefit Deductions |
      | result_title_cy             | Cais am dynnu arian o fudd-daliadau     |
      | active                      | true                                    |
      | result_type                 | Result                                  |
      | imposition_creditor         |                                         |
      | imposition_allocation_order |                                         |
