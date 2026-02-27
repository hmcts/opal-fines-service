@Opal
Feature: PO-559 get draft account

  @PO-559 @cleanUpData @PO-2360
  Scenario: Get draft account - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Submitted            |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |
      | account.originator_type             | TFO                  |

    Then The draft account response returns 200
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere

    Then the logging service contains these PDPO logs:
      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
      | 500000000        | OPAL_USER_ID    | Get Draft Account - Defendant               | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

    Then I delete the created draft accounts

  @PO-2360 @cleanUpData
  Scenario: Get draft account - Parent + MinorCreditor yields two PDPO logs
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fines                                        |
      | account_status    |                                             |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fines                 |
      | account_status                      | Submitted            |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |
      | account.originator_type             | TFO                  |

    Then The draft account response returns 200
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere

    Then the logging service contains these PDPO logs:
      | created_by_id    | created_by_type | business_identifier                         | individual_id                |expected_count |
      | 500000000        | OPAL_USER_ID    | Get Draft Account - Defendant               | <CREATED_DRAFT_ACCOUNT_ID>   |1              |
      | 500000000        | OPAL_USER_ID    | Get Draft Account - Parent or Guardian      | <CREATED_DRAFT_ACCOUNT_ID>   |1              |

    Then I delete the created draft accounts


  @PO-2360 @cleanUpData
  Scenario: Invalid token is blocked and no PDPO logs emitted
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

  # switch to a non-OPAL user/token
    When I set an invalid token manually
    And I get the single created draft account
    Then The draft account response returns 401

  # confirm no PDPO logs were emitted for this attempted GET (no side-effects)
    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"

  # switch back to an OPAL user so cleanup can delete the created draft (or delete via admin API)
    Given I am testing as the "opal-test@hmcts.net" user
    Then I delete the created draft accounts

  @PO-2360 @cleanUpData
  Scenario: Attempt to create a draft with an invalid token - no logs created
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to create a draft account with an invalid token using created by ID "invalidToken"
    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"
    Then I delete the created draft accounts


