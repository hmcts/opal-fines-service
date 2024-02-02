@Smoke @PO-149
Feature: Tests to confirm unauthenticated requests are not authenticated

  Scenario: Unauthorised request returns 401
    When I make an unauthenticated request to the defendant account details api with
      | defendantID | 500000009 |
    Then the response from the defendant account details api is unauthorised

  Scenario: Invalid token request returns 401
    When I make a request to the defendant account details api with an invalid token
      | defendantID | 500000009 |
    Then the response from the defendant account details api is unauthorised
