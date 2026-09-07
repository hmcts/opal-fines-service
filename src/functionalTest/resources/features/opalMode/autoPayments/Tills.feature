@Opal @R1CPayment @JIRA-LABEL:auto-payments @JIRA-EPIC:PO-2532
Feature: Till summary retrieval

  @JIRA-STORY:PO-2575 @JIRA-TEST-KEY:PO-2575-E2E-01
  Scenario: Tills retrieval rejects missing access token
    When I call GET "/tills?business_unit_ids=78" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2575 @JIRA-TEST-KEY:PO-2575-E2E-01
  Scenario: Tills retrieval rejects invalid access token
    When I call GET "/tills?business_unit_ids=78" with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2575 @JIRA-TEST-KEY:PO-2575-E2E-01
  Scenario: User without Process and Allocate Payments permission receives no tills
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request tills for business unit 78
    Then the tills response is an empty successful response

  @Ignore @JIRA-STORY:PO-2575 @JIRA-TEST-KEY:PO-2575-E2E-02
  Scenario: Auto-payment tills include the documented original file details
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request auto-payment tills
    Then the auto-payment tills response contains documented till and original file details

  @JIRA-STORY:PO-2575 @JIRA-TEST-KEY:PO-2575-E2E-03
  Scenario: Unmatched allocated auto-payment filters return an empty successful response
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request allocated auto-payment tills for unmatched business unit 32767
    Then the tills response is an empty successful response
