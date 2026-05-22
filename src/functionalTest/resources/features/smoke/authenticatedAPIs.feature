@Smoke @JIRA-STORY:PO-149 @JIRA-EPIC:PO-2233
Feature: Authenticated APIs Reject Invalid Credentials

  Scenario Outline: Requests without a token are rejected for <method> <path>
    When I call <method> "<path>" without a token
    Then the request is rejected as unauthorized

    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |

    Examples:
      | method | path                                   |
      | GET    | /courts?q=magistrates&business_unit=43 |

  Scenario Outline: Requests with an invalid token are rejected for <method> <path>
    When I call <method> "<path>" with an invalid token
    Then the request is rejected as unauthorized

    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |

    Examples:
      | method | path                                   |
      | GET    | /courts?q=magistrates&business_unit=43 |
