@Opal @JIRA-LABEL:manual-account-creation
Feature: Retrieve Draft Account

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-591 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-STORY:PO-2360 @JIRA-LABEL:personal-data-processing-logging
  Scenario: An existing draft account can be retrieved
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    Then the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Submitted    |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |
      | account.originator_type             | TFO          |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
    #      | 500000000        | OPAL_USER_ID    | Get Draft Account - Defendant               | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

  @JIRA-STORY:PO-2360 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355
  Scenario: A draft account with multiple related parties can be retrieved
    And a draft account exists with the following details
      | business_unit_id  | 73                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    |                                                        |
      | submitted_by      | BUUID                                                  |
      | submitted_by_name | Laura Clerk                                            |
    Then the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Submitted    |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |
      | account.originator_type             | TFO          |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
    #      | 500000000        | OPAL_USER_ID    | Get Draft Account - Defendant               | <CREATED_DRAFT_ACCOUNT_ID>   |1              |
    #      | 500000000        | OPAL_USER_ID    | Get Draft Account - Parent or Guardian      | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

  @JIRA-STORY:PO-2360 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355
  Scenario: An invalid token cannot retrieve a created draft account
    And a draft account exists with the following details
      | business_unit_id  | 73                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | BUUID                                                  |
      | submitted_by_name | Laura Clerk                                            |
    When I attempt to retrieve the created draft account with an invalid token
    Then the request is rejected as unauthorized

  # confirm no PDPO logs were emitted for this attempted GET (no side-effects)
  # Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"

  @JIRA-STORY:PO-2360 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355
  Scenario: An invalid token cannot create a draft account in the retrieval flow
    When I attempt to create a draft account with an invalid token using created by ID "invalidToken"
    Then the request is rejected as unauthorized
    #    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"
