@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:personal-data-processing-logging
Feature: Create Draft Accounts

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6019
  Scenario: Create an adult fine draft account
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | L106C2                                      |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then the draft account is created successfully with the following data
      | business_unit_id                    | 73                               |
      | account_type                        | Fine                             |
      | account_status                      | Submitted                        |
      | account_snapshot.defendant_name     | LNAME, FNAME                     |
      | account_snapshot.date_of_birth      | 2000-01-01                       |
      | account_snapshot.account_type       | Fine                             |
      | account_snapshot.submitted_by       | L073JG                           |
      | account_snapshot.submitted_by_name  | opal-test@dev.platform.hmcts.net |
      | account_snapshot.business_unit_name | West London                      |
      | account.originator_type             | TFO                              |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                         | individual_id                | expected_count |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Defendant            |<CREATED_DRAFT_ACCOUNT_ID>    | 1              |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6022
  Scenario: Create a parent or guardian draft account
    When I create a draft account with the following details
      | business_unit_id  | 77                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | PG1234                                                 |
      | submitted_by_name | opal-test                                              |
      | timeline_data     | draftAccounts/timelineJson/default.json                |

    Then the draft account is created successfully with the following data
      | business_unit_id                   | 77                               |
      | account_type                       | Fine                             |
      | account_status                     | Submitted                        |
      | account_snapshot.defendant_name    | LNAME, FNAME                     |
      | account_snapshot.date_of_birth     | 2000-01-01                       |
      | account_snapshot.account_type      | Fine                             |
      | account_snapshot.submitted_by      | L077JG                           |
      | account_snapshot.submitted_by_name | opal-test@dev.platform.hmcts.net |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                       | individual_id                | expected_count |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Defendant          | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Parent or Guardian | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6025
  Scenario: Create a minor creditor draft account
    When I create a draft account with the following details
      | business_unit_id  | 77                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | MC1234                                              |
      | submitted_by_name | opal-test                                           |
      | timeline_data     | draftAccounts/timelineJson/default.json             |

    Then the draft account is created successfully with the following data
      | business_unit_id                   | 77                               |
      | account_type                       | Fine                             |
      | account_status                     | Submitted                        |
      | account_snapshot.defendant_name    | LNAME, FNAME                     |
      | account_snapshot.account_type      | Fine                             |
      | account_snapshot.submitted_by      | L077JG                           |
      | account_snapshot.submitted_by_name | opal-test@dev.platform.hmcts.net |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
    #      | 500000000        | OPAL_USER_ID    | Submit Draft Account - Defendant            | <CREATED_DRAFT_ACCOUNT_ID>   |1              |
    #      | 500000000        | OPAL_USER_ID    | Submit Draft Account - Minor Creditor       | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6028
  Scenario: Reject draft-account creation with an invalid token
    When I attempt to create a draft account with an invalid token using created by ID "invalidToken"
    #    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Submit Draft Account - Defendant"
