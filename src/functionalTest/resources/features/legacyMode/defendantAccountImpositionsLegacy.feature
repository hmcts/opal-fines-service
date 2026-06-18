@Legacy @Ignore @JIRA-STORY:PO-2078 @JIRA-EPIC:PO-979
Feature: Defendant Account Impositions API In Legacy Mode

  Scenario: Existing defendant account impositions are returned from the legacy stub
    When I request legacy defendant account impositions for defendant account "12345"

    Then the request succeeds
    And the legacy defendant account impositions response has ETag "\"1\""
    And the legacy defendant account impositions response contains
      | impositions[0].date_added                       | 2026-05-06                               |
      | impositions[0].imposition.result_id             | ABDC                                     |
      | impositions[0].imposition.result_title          | Application made for Benefit Deductions |
      | impositions[0].creditor.creditor_account_id     | 99000000000806                          |
      | impositions[0].creditor.account_type            | MN                                       |
      | impositions[0].creditor.display_name            | Minor Creditor                          |
      | impositions[0].creditor.minor_creditor_party_id | 99000000000906                          |
      | impositions[0].creditor.name                    | Metropolitan Traffic Unit               |
      | impositions[0].offence.code                     | OFF0006                                  |
      | impositions[0].offence.title                    | Test Offence 6                           |
      | impositions[0].imposition_id                    | 99000000003006                          |
