@Opal @R1CPayment @JIRA-LABEL:auto-payments @JIRA-EPIC:PO-2116
Feature: Allocate tills

  # GET /tills exposes till_id and PO-10810 provides manual-till cleanup. These scenarios verify
  # that tills are Processing after a successful request; integration tests verify the database
  # transition and broker interaction. Allocation completion is downstream behavior, outside this endpoint.

  @JIRA-STORY:PO-3423
  Scenario: Till allocation rejects a missing access token
    When I call POST "/tills/allocate" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3423
  Scenario: Till allocation rejects an invalid access token
    When I call POST "/tills/allocate" with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3423
  Scenario: Till allocation requires Process and Allocate Payments permission
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I submit a till allocation request as a user without permission
    Then the request is rejected as forbidden

  @JIRA-STORY:PO-3423
  Scenario Outline: Till allocation rejects a request outside the API schema
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit an invalid till allocation request "<request>"
    Then the request is rejected as bad request

    Examples:
      | request                       |
      | without tills                        |
      | with an empty tills list             |
      | with a till missing its ID           |
      | with a till missing its business unit |
      | with a null till entry                |

  @JIRA-STORY:PO-3423
  Scenario: Till allocation returns not found for an unknown till
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit an allocation request for a till that does not exist
    Then the request is rejected as not found

  @Ignore @JIRA-DEFECT:PO-10560 @JIRA-STORY:PO-3423
  Scenario: Till allocation rejects a till submitted under the wrong business unit without partial updates
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create 2 uniquely identifiable tills for allocation in business unit 78
    When I submit the created tills for allocation with a mismatched business unit
    Then the request is rejected as bad request
    And all created tills are listed with status "CREATED"
    And I remove all created tills using the Testing Support API

  @Ignore @JIRA-DEFECT:PO-10560 @JIRA-STORY:PO-3423
  Scenario: An authorised user queues a created till
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create 1 uniquely identifiable till for allocation in business unit 78
    And all created tills are listed with status "CREATED"
    When I submit the created tills for allocation
    Then till allocation succeeds with no response body
    And all created tills are listed with status "PROCESSING"
    And I remove all created tills using the Testing Support API

  @Ignore @JIRA-DEFECT:PO-10560 @JIRA-STORY:PO-3423
  Scenario: An authorised user queues multiple created tills in one request
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create 2 uniquely identifiable tills for allocation in business unit 78
    Then all created tills are listed with status "CREATED"
    When I submit the created tills for allocation
    Then till allocation succeeds with no response body
    And all created tills are listed with status "PROCESSING"
    And I remove all created tills using the Testing Support API

  # Broker-level tests verify one message per till and a shared session ID for a batch.

  # The API has no public or Testing Support operation that places a till in Failed status.
  # The Failed-to-Processing transition is covered by PO-3423 integration tests until a stable
  # functional fixture for a Failed till is agreed.

  # @R1CPaymentOff selects this scenario only; run it against a service configured with the payment flag off.
  @R1CPaymentOff @JIRA-STORY:PO-3423
  Scenario: Till allocation is unavailable when Release 1C payments are disabled
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit a till allocation request while Release 1C payments are disabled
    Then the request is rejected as not found
    And the Release 1C payment feature-disabled response is returned
