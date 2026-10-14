@Opal @R1CPayment @JIRA-LABEL:auto-payments @JIRA-EPIC:PO-2532
Feature: Till summary retrieval

  # E2E.01 — Missing access token returns 401.
  @JIRA-STORY:PO-2575
  Scenario: Tills retrieval rejects missing access token
    When I call GET "/tills?business_unit_ids=78" without a token
    Then the request is rejected as unauthorized

  # E2E.01 — Invalid access token returns 401.
  @JIRA-STORY:PO-2575
  Scenario: Tills retrieval rejects invalid access token
    When I call GET "/tills?business_unit_ids=78" with an invalid token
    Then the request is rejected as unauthorized

  # E2E.01 — No BU filter and no payment permission returns an empty result.
  @JIRA-STORY:PO-10713
  Scenario: User without Process and Allocate Payments permission receives no tills when the BU filter is omitted
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request tills without a business unit filter
    Then the tills response is an empty successful response

  # E2E.02 — A processed auto-payment till includes original-file details.
  @JIRA-STORY:PO-2575 @Ignore @JIRA-DEFECT:PO-10560
  Scenario: Processed auto-payment till includes the documented original file details
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    Then the auto-payment tills response contains documented till and original file details

  # E2E.03 — An explicitly requested unauthorized BU returns forbidden.
  @JIRA-STORY:PO-10713
  Scenario: Explicitly requested unauthorized business unit is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request tills for business unit 32767
    Then the request is rejected as forbidden
