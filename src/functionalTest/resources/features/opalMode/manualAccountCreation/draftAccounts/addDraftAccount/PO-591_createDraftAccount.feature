@Opal
Feature: PO-591 create draft account / @PO-2357 validate logging

  @PO-591 @PO-2357 @cleanUpData
  Scenario: Create draft account - Adult
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fines                                       |
      | account_status    | Submitted                                   |
      | submitted_by      | L106C2                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 73                   |
      | account_type                        | Fines                |
      | account_status                      | Submitted            |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.submitted_by_name  | opal-test@HMCTS.NET  |
      | account_snapshot.business_unit_name | West London          |

    Then the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                         | expected_count |
      | L073JG        | OPAL_USER_ID    | Submit Draft Account - Defendant            | 1              |

    Then I delete the created draft accounts

  @PO-2357 @cleanUpData
  Scenario: Create draft account - parent or guardian to pay
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 77                                             |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fines                                         |
      | account_status    | Submitted                                     |
      | submitted_by      | PG1234                                        |
      | submitted_by_name | opal-test                                     |
      | timeline_data     | draftAccounts/timelineJson/default.json       |

    Then The draft account response returns 201
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 77                          |
      | account_type                        | Fines                       |
      | account_status                      | Submitted                   |
      | account_snapshot.defendant_name     | LNAME, FNAME                |
      | account_snapshot.date_of_birth      | 2000-01-01                  |
      | account_snapshot.account_type       | Fine                        |
      | account_snapshot.submitted_by       | L077JG                      |
      | account_snapshot.submitted_by_name  | opal-test@HMCTS.NET         |

    Then the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                       | expected_count |
      | L077JG        | OPAL_USER_ID    | Submit Draft Account - Defendant          | 1              |
      | L077JG        | OPAL_USER_ID    | Submit Draft Account - Parent or Guardian | 1              |

    Then I delete the created draft accounts


  @PO-2357 @cleanUpData
  Scenario: Create draft account - company with minor creditor
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 77                                                     |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fines                                                  |
      | account_status    | Submitted                                              |
      | submitted_by      | MC1234                                               |
      | submitted_by_name | opal-test                                              |
      | timeline_data     | draftAccounts/timelineJson/default.json                |

    Then The draft account response returns 201
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                   | 77                                |
      | account_type                       | Fines                             |
      | account_status                     | Submitted                         |
      | account_snapshot.defendant_name    | LNAME, FNAME                      |
      | account_snapshot.account_type      | Fine                              |
      | account_snapshot.submitted_by      | L077JG                            |
      | account_snapshot.submitted_by_name | opal-test@HMCTS.NET               |


    Then the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                         | expected_count |
      | L077JG        | OPAL_USER_ID    | Submit Draft Account - Defendant            | 1              |
      | L077JG        | OPAL_USER_ID    | Submit Draft Account - Minor Creditor       | 1              |

    Then I delete the created draft accounts

  @PO-2357 @cleanUpData
  Scenario: Attempt to create a draft with an invalid token - no logs created
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to create a draft account with an invalid token using created by ID "invalidToken"
    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Submit Draft Account - Defendant"
    Then I delete the created draft accounts
