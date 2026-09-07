@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:personal-data-processing-logging @R1A
Feature: Create Draft Accounts

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5621
  Scenario: Create an adult fine draft account
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | L106C2                                      |
      | submitted_by_name | Laura Clerk                                 |
    Then the draft account is created successfully with the following data
      | business_unit_id                    | 73                               |
      | account_type                        | Fine                             |
      | account_status                      | Submitted                        |
      | account_snapshot.defendant_name     | LNAME, FNAME                     |
      | account_snapshot.date_of_birth      | 2000-01-01                       |
      | account_snapshot.account_type       | Fine                             |
      | account_snapshot.submitted_by       | L073JG                           |
      | account_snapshot.submitted_by_name  | opal-test                        |
      | account_snapshot.business_unit_name | West London                      |
      | account.originator_type             | TFO                              |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                         | individual_id                | expected_count |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Defendant            |<CREATED_DRAFT_ACCOUNT_ID>    | 1              |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5622
  Scenario: Create a parent or guardian draft account
    When I create a draft account with the following details
      | business_unit_id  | 77                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | PG1234                                                 |
      | submitted_by_name | opal-test                                              |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 77                               |
      | account_type                       | Fine                             |
      | account_status                     | Submitted                        |
      | account_snapshot.defendant_name    | LNAME, FNAME                     |
      | account_snapshot.date_of_birth     | 2000-01-01                       |
      | account_snapshot.account_type      | Fine                             |
      | account_snapshot.submitted_by      | L077JG                           |
      | account_snapshot.submitted_by_name | opal-test                        |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                       | individual_id                | expected_count |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Defendant          | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |
    #      | 500000000     | OPAL_USER_ID    | Submit Draft Account - Parent or Guardian | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5623
  Scenario: Create a minor creditor draft account
    When I create a draft account with the following details
      | business_unit_id  | 77                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | MC1234                                              |
      | submitted_by_name | opal-test                                           |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 77                               |
      | account_type                       | Fine                             |
      | account_status                     | Submitted                        |
      | account_snapshot.defendant_name    | LNAME, FNAME                     |
      | account_snapshot.account_type      | Fine                             |
      | account_snapshot.submitted_by      | L077JG                           |
      | account_snapshot.submitted_by_name | opal-test                        |

    #    Then the logging service contains these PDPO logs:
    #      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
    #      | 500000000        | OPAL_USER_ID    | Submit Draft Account - Defendant            | <CREATED_DRAFT_ACCOUNT_ID>   |1              |
    #      | 500000000        | OPAL_USER_ID    | Submit Draft Account - Minor Creditor       | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

  @JIRA-STORY:PO-2357 @JIRA-NFR:PO-2507 @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-TEST-KEY:PO-7855
  Scenario: Emitted PDPO logs do not expose personal details
    When I create a draft account with the following details
      | business_unit_id  | 77                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | PG1234                                                 |
      | submitted_by_name | opal-test                                              |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 77                               |
      | account_type                       | Fine                             |
      | account_status                     | Submitted                        |
      | account_snapshot.defendant_name    | LNAME, FNAME                     |
      | account_snapshot.date_of_birth     | 2000-01-01                       |
      | account_snapshot.account_type      | Fine                             |
      | account_snapshot.submitted_by      | L077JG                           |
      | account_snapshot.submitted_by_name | opal-test                        |
    And the logging service emits PDPO logs for the created draft account id
    And the emitted PDPO logs do not contain these field names
      | surname         |
      | forenames       |
      | dob             |
      | email_address_1 |
      | email_address_2 |
    And the emitted PDPO logs do not contain these values
      | LNAME              |
      | Opal parent1       |
      | 2000-01-01         |
      | PGemail1@email.com |
      | PGemail2@test.com  |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - NEW fine originator resolves against a valid CRWCRT
    When I create a draft account with the following details
      | business_unit_id         | 73                                  |
      | account                  | draftAccounts/accountJson/account.json |
      | account_type             | Fine                                 |
      | account_status           | Submitted                            |
      | submitted_by             | BUUID                                |
      | submitted_by_name        | Laura Clerk                          |
      | account_originator_type   | NEW                                  |
      | account_originator_id     | 401                                  |
      | account_originator_name   | Aylesbury Crown Court                |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                    |
      | account_type                        | Fine                  |
      | account_status                      | Submitted             |
      | account.originator_type             | NEW                   |
      | account.originator_id               | 401                   |
      | account.originator_name             | Aylesbury Crown Court |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - NEW confiscation originator resolves against a valid CRWCRT
    When I create a draft account with the following details
      | business_unit_id         | 73                                  |
      | account                  | draftAccounts/accountJson/account.json |
      | account_type             | Confiscation                         |
      | account_status           | Submitted                            |
      | submitted_by             | BUUID                                |
      | submitted_by_name        | Laura Clerk                          |
      | account_originator_type   | NEW                                  |
      | account_originator_id     | 401                                  |
      | account_originator_name   | Aylesbury Crown Court                |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                    |
      | account_type                        | Confiscation          |
      | account_status                      | Submitted             |
      | account.originator_type             | NEW                   |
      | account.originator_id               | 401                   |
      | account.originator_name             | Aylesbury Crown Court |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - NEW conditional caution originator resolves against a prosecutor
    When I create a draft account with the following details
      | business_unit_id         | 73                                                    |
      | account                  | draftAccounts/accountJson/account.json                 |
      | account_type             | Conditional Caution                                   |
      | account_status           | Submitted                                             |
      | submitted_by             | BUUID                                                 |
      | submitted_by_name        | Laura Clerk                                           |
      | account_originator_type   | NEW                                                   |
      | account_originator_id     | 1                                                     |
      | account_originator_name   | Met Camera Processing Services / Traffic Offence Reports |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                                                      |
      | account_type                        | Conditional Caution                                     |
      | account_status                      | Submitted                                               |
      | account.originator_type             | NEW                                                     |
      | account.originator_id               | 1                                                       |
      | account.originator_name             | Met Camera Processing Services / Traffic Offence Reports |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - TFO fine originator resolves against a valid LJA
    When I create a draft account with the following details
      | business_unit_id         | 73                                  |
      | account                  | draftAccounts/accountJson/account.json |
      | account_type             | Fine                                 |
      | account_status           | Submitted                            |
      | submitted_by             | BUUID                                |
      | submitted_by_name        | Laura Clerk                          |
      | account_originator_type   | TFO                                  |
      | account_originator_id     | 3190                                 |
      | account_originator_name   | Cardiff Magistrates' Court           |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                    |
      | account_type                        | Fine                  |
      | account_status                      | Submitted             |
      | account.originator_type             | TFO                   |
      | account.originator_id               | 3190                  |
      | account.originator_name             | Cardiff Magistrates' Court |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - TFO fixed penalty originator resolves against a prosecutor
    When I create a draft account with the following details
      | business_unit_id         | 73                                                    |
      | account                  | draftAccounts/accountJson/account.json                 |
      | account_type             | Fixed Penalty                                         |
      | account_status           | Submitted                                             |
      | submitted_by             | BUUID                                                 |
      | submitted_by_name        | Laura Clerk                                           |
      | account_originator_type   | TFO                                                   |
      | account_originator_id     | 1                                                     |
      | account_originator_name   | Met Camera Processing Services / Traffic Offence Reports |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                                                      |
      | account_type                        | Fixed Penalty                                           |
      | account_status                      | Submitted                                               |
      | account.originator_type             | TFO                                                     |
      | account.originator_id               | 1                                                       |
      | account.originator_name             | Met Camera Processing Services / Traffic Offence Reports |

  @JIRA-STORY:PO-5742 @JIRA-EPIC:PO-8248 @cleanUpData
  Scenario: Create draft account - FP fixed penalty originator resolves against a prosecutor
    When I create a draft account with the following details
      | business_unit_id         | 73                                                    |
      | account                  | draftAccounts/accountJson/account.json                 |
      | account_type             | Fixed Penalty                                         |
      | account_status           | Submitted                                             |
      | submitted_by             | BUUID                                                 |
      | submitted_by_name        | Laura Clerk                                           |
      | account_originator_type   | FP                                                    |
      | account_originator_id     | 1                                                     |
      | account_originator_name   | Met Camera Processing Services / Traffic Offence Reports |
    Then the draft account is created successfully with the following data
      | business_unit_id                   | 73                                                      |
      | account_type                        | Fixed Penalty                                           |
      | account_status                      | Submitted                                               |
      | account.originator_type             | FP                                                      |
      | account.originator_id               | 1                                                       |
      | account.originator_name             | Met Camera Processing Services / Traffic Offence Reports |

  @JIRA-STORY:PO-559 @JIRA-STORY:PO-2357 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-TEST-KEY:PO-5624
  Scenario: Reject draft-account creation with an invalid token
    When I attempt to create a draft account with an invalid token
    #    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Submit Draft Account - Defendant"
