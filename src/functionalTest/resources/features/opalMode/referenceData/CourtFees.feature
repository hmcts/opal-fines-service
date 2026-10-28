@Opal @R1CPayment @JIRA-LABEL:reference-data @JIRA-EPIC:PO-2660
Feature: Court Fees Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  # AC1 and AC4: authentication is required and failures use the standard API response.
  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests require an access token
    When I call GET "/court-fees?business_unit_id=73" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests reject an invalid access token
    When I call GET "/court-fees?business_unit_id=73" with an invalid token
    Then the request is rejected as unauthorized

  # AC1 and AC2: an authorised business unit receives court-fee reference data in the API shape.
  @JIRA-STORY:PO-3701
  Scenario: Court fees can be retrieved for an authorised business unit
    When I request court fees for business unit 73
    Then the court fees response contains reference data

  # AC2: an authorised business unit without court fees returns an empty array.
  @JIRA-STORY:PO-3701
  Scenario: An authorised business unit with no court fees returns an empty list
    When I request court fees for business unit 77
    Then the court fees response is an empty list

  # TODO PO-TBC: Revisit the expected status when required-parameter handling is aligned across endpoints.
  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests require a valid business-unit query parameter
    When I request court fees without a business unit
    Then the court fees request is rejected with standard problem status 500 and title "Internal Server Error"

  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests reject a non-numeric business-unit query parameter
    When I request court fees with business unit "not-a-number"
    Then the court fees request is rejected with standard problem status 406 and title "Not Acceptable"

  # AC1: the user must be authorised for the requested business unit.
  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests are forbidden for an unauthorised business unit
    When I request court fees for business unit 36
    Then the court fees request is rejected with standard problem status 403 and title "Forbidden"

  # AC1 and AC4: unknown business units use the standard not-found response.
  @JIRA-STORY:PO-3701
  Scenario: Court-fee requests return not found for an unknown business unit
    When I request court fees for business unit 999
    Then the court fees request is rejected with standard problem status 404 and title "Entity Not Found"

  # Feature flag AC: run this scenario in the all Release 1C payment flags-off functional profile.
  @R1CPaymentOff @JIRA-STORY:PO-3701
  Scenario: Court fees are unavailable when Release 1C payment is disabled
    When I request court fees for business unit 73
    Then the court fees feature-disabled problem response is returned
