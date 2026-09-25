@Legacy @R1BDrop1 @JIRA-STORY:PO-2078 @JIRA-EPIC:PO-979
Feature: Defendant Account Impositions API In Legacy Mode

  Scenario: Existing defendant account impositions are returned from the legacy stub
    When I request legacy defendant account impositions for defendant account "99000000000006"

    Then the request succeeds
    And the legacy defendant account impositions response has ETag "\"18338687664539878704807506660830801130000030354\""
    And the legacy defendant account impositions response contains
      | impositions[0].date_added                   | 2026-08-19                         |
      | impositions[0].date_imposed                 | 2025-05-15                         |
      | impositions[0].imposition.result_id         | FO                                 |
      | impositions[0].imposition.result_title      | FINE                               |
      | impositions[0].creditor.creditor_account_id | 77                                 |
      | impositions[0].creditor.account_type        | CF                                 |
      | impositions[0].creditor.display_name        | Central Fund                       |
      | impositions[0].creditor.name                | HM Courts & Tribunals Service      |
      | impositions[0].imposed_amount               | -250.0                             |
      | impositions[0].paid_amount                  | 300.0                              |
      | impositions[0].balance                      | 50.0                               |
      | impositions[0].offence.id                   | 33369                              |
      | impositions[0].offence.code                 | HY35014                            |
      | impositions[0].offence.title                | Riding a bicycle on a footpath     |
      | impositions[0].imposition_id                | 770000027211                       |
