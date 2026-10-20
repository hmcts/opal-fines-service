@Opal @R1CPayment @JIRA-LABEL:manual-payments @JIRA-EPIC:PO-2439
Feature: Create till

  # AC1 — Authentication is required before a create request is accepted.
  @JIRA-STORY:PO-3630
  Scenario: Till creation rejects a missing access token
    When I call POST "/tills" without a token
    Then the request is rejected as unauthorized

  # AC1 — Authentication is required before a create request is accepted.
  @JIRA-STORY:PO-3630
  Scenario: Till creation rejects an invalid access token
    When I call POST "/tills" with an invalid token
    Then the request is rejected as unauthorized

  # AC1 — The user must hold Process and Allocate Payments in the request business unit.
  @JIRA-STORY:PO-3630
  Scenario: Till creation rejects a user without payment permission in the requested business unit
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I submit a valid request to create a till
    Then the request is rejected as forbidden

  # AC1 — The request must include the business unit and a non-empty payments list.
  @JIRA-STORY:PO-3630
  Scenario Outline: Till creation rejects a request that does not conform to the API specification
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit a till creation request <invalid_request>
    Then the request is rejected as bad request

    Examples:
      | invalid_request                    |
      | without its business unit           |
      | with no payments                    |
      | with payment details omitted        |

  # AC Feature Flags — This scenario is run only by the all-flags-off functional profile.
  @R1CPaymentOff @JIRA-STORY:PO-3630
  Scenario: Till creation is unavailable when the Release 1C payment feature is disabled
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I submit a valid request to create a till
    Then the request is rejected as not found
    And the Release 1C payment feature-disabled response is returned

  # AC2 — Provisional until PO-10713 exposes till_id in GET /tills and the agreed Testing Support
  # cleanup route is available. Date and receipt are deliberately
  # excluded from the request because they are absent from the delivered PO-8948 API schema.
  @Ignore @JIRA-STORY:PO-3630
  Scenario: An authorised user creates a till with one criminal payment
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I record the existing till identifiers for business unit 78
    When I submit a uniquely identifiable valid request to create a till with one criminal payment
    Then the request creates a resource
    And I can retrieve the newly created till
    And the till belongs to business unit 78 and contains one linked criminal payment
    And the payment preserves its defendant account, amount, method, destination type, allocation type and third-party payer name
    And I remove the created till using the Testing Support API

  # AC2 — Provisional companion scenario proving a till persists every payment submitted in one request.
  @Ignore @JIRA-STORY:PO-3630
  Scenario: An authorised user creates all criminal payments submitted for a till
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I record the existing till identifiers for business unit 78
    When I submit a uniquely identifiable valid request to create a till with two criminal payments
    Then the request creates a resource
    And I can retrieve the newly created till
    And the till belongs to business unit 78 and contains two linked criminal payments
    And I remove the created till using the Testing Support API
