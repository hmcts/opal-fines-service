@Smoke @PO-149
Feature: Tests to confirm unauthenticated requests are not authenticated

  Scenario Outline: Unauthenticated requests are rejected
    When I call <method> "<path>" without a token
    Then the response status is 401

    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |
      | GET    | /courts?q=magistrates&business_unit=43 |

  Scenario Outline: Invalid token is rejected
    When I call <method> "<path>" with an invalid token
    Then the response status is 401

    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |
      | GET    | /courts?q=magistrates&business_unit=43 |
