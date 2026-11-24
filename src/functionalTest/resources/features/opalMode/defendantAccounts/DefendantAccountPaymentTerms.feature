@Opal @R1BDrop1 @JIRA-LABEL:account-enquiry
Feature: Defendant account payment-term amendments

  # Reason for change is exposed by the API as reason_for_extension.
  # Instalments with an initial lump sum use type I, as do instalments without one.
  # Paid terms are not an editable payment arrangement for an outstanding account.
  # Expected to fail in Legacy mode until PO-11822 is fixed: the latest GET returns a null reason for change.
  @cleanUpData @JIRA-STORY:PO-1718 @JIRA-EPIC:PO-977
  @JIRA-DEFECT:PO-11822
  Scenario Outline: Maximum-length reason is preserved for <shape> payment terms
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source                       | MAC                                         |
      | business_unit_id             | 77                                          |
      | account_enforcement_court_id | 770000000001                                |
      | account                      | draftAccounts/accountJson/adultAccount.json |
      | account_type                 | Fine                                        |
      | account_status               | Submitted                                   |
      | submitted_by                 | PAYTERMS001                                 |
      | submitted_by_name            | Laura Clerk                                 |
    And a payment-term amendment fixture with a 250-character reason and the following values
      | payment_terms_type_code         | <type>        |
      | payment_terms_type_display_name | <type_name>   |
      | instalment_period_code          | <period>      |
      | instalment_period_display_name  | <period_name> |
      | lump_sum_amount                 | <lump_sum>    |
      | instalment_amount               | <instalment>  |
      | days_in_default                 | 99999         |
    When I submit the maximum-length payment-term amendment for business unit "77"
    Then the latest payment terms match the submitted amendment including the reason content and length

    Examples:
      | shape                            | type | type_name   | period | period_name | lump_sum | instalment |
      | by date                          | B    | By date     | null   | null        | 100.50   | null       |
      | weekly instalments               | I    | Instalments | W      | Weekly      | null     | 25.50      |
      | fortnightly instalments          | I    | Instalments | F      | Fortnightly | null     | 30.25      |
      | monthly instalments              | I    | Instalments | M      | Monthly     | null     | 45.75      |
      | lump sum and weekly instalments   | I    | Instalments | W      | Weekly      | 100.50   | 25.50      |
      | lump sum and fortnightly payments | I    | Instalments | F      | Fortnightly | 100.50   | 30.25      |
      | lump sum and monthly instalments  | I    | Instalments | M      | Monthly     | 100.50   | 45.75      |
