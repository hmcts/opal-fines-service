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

  # E2E.01 — Valid user without payment permission receives an empty result.
  @JIRA-STORY:PO-2575
  Scenario: User without Process and Allocate Payments permission receives no tills
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request tills for business unit 78
    Then the tills response is an empty successful response

  # E2E.02 — A processed auto-payment till includes original-file details.
  @Ignore @JIRA-STORY:PO-2575
  Scenario: Processed auto-payment till includes the documented original file details
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And I create and process an auto-payment interface job for business unit 77
    When I request the generated auto-payment till
    Then the auto-payment tills response contains documented till and original file details

  # E2E.03 — Unmatched filters return the empty success contract.
  @JIRA-STORY:PO-2575
  Scenario: Unmatched allocated auto-payment filters return an empty successful response
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request allocated auto-payment tills for unmatched business unit 32767
    Then the tills response is an empty successful response
