@Legacy @R1BDrop1 @JIRA-STORY:PO-2078 @JIRA-EPIC:PO-979
Feature: Defendant Account Impositions API In Legacy Mode

  Scenario: Existing defendant account impositions are returned from the legacy stub
    When I request legacy defendant account impositions for defendant account "770000006113"

    Then the request succeeds
    And the response must include a strong quoted ETag header
    # The returned imposition ID selects one complete known preprod or JCDE dataset.
    And the legacy defendant account impositions contain the expected data for the target environment
      | field                                                                  | preprod                                                                                                   | jcde                             |
      | imposition_id                                                          | 770000047400                                                                                              | 770000031790                     |
      | date_added                                                             | 2020-01-16                                                                                                | 2026-10-07                       |
      | date_imposed                                                           | 2020-01-16                                                                                                | 2025-05-15                       |
      | imposition.result_id                                                   | FCOMP                                                                                                     | FO                               |
      | imposition.result_title                                                | COMPENSATION                                                                                              | FINE                             |
      | creditor.creditor_account_id                                           | 10770000000041                                                                                            | 77                               |
      | creditor.creditor_account_type_reference.creditor_account_type         | MJ                                                                                                        | CF                               |
      | creditor.creditor_account_type_reference.creditor_account_display_name | Major Creditor                                                                                            | Central Fund                     |
      | creditor.major_creditor_name                                           | TFL2 ATCM Testing                                                                                         | HM Courts & Tribunals Service    |
      | imposed_amount                                                         | 5.0                                                                                                       | 1250.0                           |
      | paid_amount                                                            | 0.0                                                                                                       | 10.0                             |
      | balance                                                                | -5.0                                                                                                      | -1240.0                          |
      | offence.offence_id                                                     | 49802                                                                                                     | 32043                            |
      | offence.cjs_code                                                       | TR11013                                                                                                   | TH68001                          |
      | offence.offence_title                                                  | Tout for / solicit money / reward without permission on the Transport for London regional railway network | Theft from the person of another |
