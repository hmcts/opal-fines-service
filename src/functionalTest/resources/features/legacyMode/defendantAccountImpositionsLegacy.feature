@Legacy @R1BDrop1 @JIRA-STORY:PO-2078 @JIRA-EPIC:PO-979
Feature: Defendant Account Impositions API In Legacy Mode

  Scenario: Existing defendant account impositions are returned from the legacy stub
    When I request legacy defendant account impositions for defendant account "730000004745"

    Then the request succeeds
    And the legacy defendant account impositions response has ETag "\"1914683492368317065449543989290119028490000013512\""
    And the legacy defendant account impositions response contains
      | impositions[0].date_added                                                         | 2026-06-05                         |
      | impositions[0].date_imposed                                                       | 2026-01-01                         |
      | impositions[0].imposition.result_id                                               | FCOMP                              |
      | impositions[0].imposition.result_title                                            | COMPENSATION                       |
      | impositions[0].creditor.creditor_account_id                                       | 20730000001166                     |
      | impositions[0].creditor.creditor_account_type_reference.creditor_account_type      | MN                                 |
      | impositions[0].creditor.creditor_account_type_reference.creditor_account_display_name | Minor Creditor                  |
      | impositions[0].creditor.minor_creditor_organisation_flag                          | false                              |
      | impositions[0].creditor.individual_name.forenames                                 | Davey                              |
      | impositions[0].creditor.individual_name.surname                                   | Davidson                           |
      | impositions[0].imposed_amount                                                     | 10.0                               |
      | impositions[0].paid_amount                                                        | 0.0                                |
      | impositions[0].balance                                                            | -10.0                              |
      | impositions[0].offence.offence_id                                                 | 38158                              |
      | impositions[0].offence.cjs_code                                                   | CJ94017                            |
      | impositions[0].offence.offence_title                                              | Aggravated trespass - fail to leave land |
      | impositions[0].imposition_id                                                      | 730000013514                       |
