@Opal @JIRA-LABEL:reference-data
Feature: Mappings Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1BDrop1 @JIRA-TEST-KEY:PO-9551
  Scenario: A mappings request without a token is rejected
    When I call GET "/mappings/defendant-account-status" without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1BDrop1 @JIRA-TEST-KEY:PO-9552
  Scenario: A mappings request with an invalid token is rejected
    When I call GET "/mappings/defendant-account-status" with an invalid token
    Then the request is rejected as unauthorized

  # PO-10964 AC6: existing defendant account status mappings remain unchanged.
  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @R1BDrop1 @JIRA-TEST-KEY:PO-9553
  @JIRA-STORY:PO-10964
  Scenario: Defendant account status mappings can be retrieved
    When I make a request to the mappings api for type "defendant-account-status"
    Then the defendant account status mappings are returned

  # PO-10964 AC8: unsupported mapping errors include the complete supported type list in alphabetical order.
  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @JIRA-TEST-KEY:PO-9554
  @R1CPayment @JIRA-STORY:PO-10964
  Scenario: A mappings request with an unsupported type is rejected as bad request
    When I make a request to the mappings api for type "unsupported-type"
    Then the request is rejected as bad request
    And the mappings error response lists the supported types alphabetically

  # PO-10964 AC8: missing mapping type errors include the complete supported type list in alphabetical order.
  @JIRA-STORY:PO-3871 @JIRA-EPIC:PO-3372 @JIRA-TEST-KEY:PO-9555
  @R1CPayment @JIRA-STORY:PO-10964
  Scenario: A mappings request without a type is rejected as bad request
    When I make a request to the mappings api without a type
    Then the request is rejected as bad request
    And the mappings error response lists the supported types alphabetically

  @JIRA-STORY:PO-10576 @JIRA-EPIC:PO-3516
  Scenario: All the mappings are returned for file-type
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the mappings api for type "file-type"
    Then the status mappings are returned with code, display name and mime type

  # PO-10964 new payment mapping scenarios
  # AC1, AC4, AC5, AC10: method of payment returns only the exact values when release-1c-payment is enabled.
  @R1CPayment @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Method of payment mappings can be retrieved
    When I make a request to the mappings api for type "method-of-payment"
    Then the method of payment mappings are returned

  # AC2, AC4, AC5, AC10: source returns only the exact values when release-1c-payment is enabled.
  @R1CPayment @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Source mappings can be retrieved
    When I make a request to the mappings api for type "source"
    Then the source mappings are returned

  # AC3, AC4, AC5, AC10: suspense reason returns only the exact values when release-1c-payment is enabled.
  @R1CPayment @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Suspense reason mappings can be retrieved
    When I make a request to the mappings api for type "suspense-reason"
    Then the suspense reason mappings are returned

  # AC11: each new mapping type returns the standard feature-disabled problem when release-1c-payment is disabled.
  @R1CPaymentOff @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario Outline: Payment mappings are unavailable when release-1c-payment is disabled
    When I make a request to the mappings api for type "<type>"
    Then the release-1c-payment disabled mapping response is returned

    Examples:
      | type               |
      | method-of-payment  |
      | source             |
      | suspense-reason    |

  # AC12: defendant account status remains available with Release 1B enabled while release-1c-payment is disabled.
  @R1BDrop1 @R1CPaymentOff @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Defendant account status mappings remain available with release-1c-payment disabled
    When I make a request to the mappings api for type "defendant-account-status"
    Then the defendant account status mappings are returned

  # AC13: supported_types omits payment mappings when release-1c-payment is disabled.
  @R1BDrop1 @R1CPaymentOff @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Unsupported mapping errors list only Release 1B types when release-1c-payment is disabled
    When I make a request to the mappings api for type "unsupported-type"
    Then the request is rejected as bad request
    And the disabled mappings error response lists only Release 1B types

  # AC13: supported_types omits payment mappings for a missing type when release-1c-payment is disabled.
  @R1BDrop1 @R1CPaymentOff @JIRA-STORY:PO-10964 @JIRA-EPIC:PO-2439
  Scenario: Missing mapping errors list only Release 1B types when release-1c-payment is disabled
    When I make a request to the mappings api without a type
    Then the request is rejected as bad request
    And the disabled mappings error response lists only Release 1B types
