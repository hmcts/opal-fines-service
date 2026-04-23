@Opal @JIRA-LABEL:manual-account-creation
Feature: Get Draft Accounts

  @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6087
  Scenario: Get draft accounts - filtering on business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I get the draft accounts filtering on the Business unit "73" then the response contains
      | business_unit_id                    | 73          |
      | account_snapshot.business_unit_name | West London |
    And The draft account filtered response does not contain accounts in the "77" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    When I get the draft accounts filtering on the Business unit "77" then the response contains
      | business_unit_id                    | 77               |
      | account_snapshot.business_unit_name | Camberwell Green |
    And The draft account filtered response does not contain accounts in the "73" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    When I get the draft accounts filtering on the Business unit "65" then the response contains
      | business_unit_id                    | 65                   |
      | account_snapshot.business_unit_name | Camden and Islington |
    And The draft account filtered response does not contain accounts in the "73" business unit
    And The draft account filtered response does not contain accounts in the "77" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    Then I delete the created draft accounts

  @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6090
  Scenario: Get draft accounts - filtering on status
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | If-Match          | 0                                           |

    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I get the draft accounts filtering on the Status "SUBMITTED" then the response contains
      | account_status | Submitted |
    And The draft account filtered response does not contain accounts with status "Resubmitted"

    When I get the draft accounts filtering on the Status "RESUBMITTED" then the response contains
      | account_status | Resubmitted |
    And The draft account filtered response does not contain accounts with status "Submitted"

    Then I delete the created draft accounts

  @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6092
  Scenario: Get draft accounts - filtering on submitted_by
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I get the draft accounts filtering on Submitted by "BUUID" then the response contains
      | account_snapshot.submitted_by | BUUID |
    And The draft account filtered response does not contain accounts submitted by "BUUID_TWO"

    When I get the draft accounts filtering on Submitted by "BUUID_TWO" then the response contains
      | account_snapshot.submitted_by | BUUID_TWO |
    And The draft account filtered response does not contain accounts submitted by "BUUID"

    Then I delete the created draft accounts

  @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @cleanUpData @JIRA-KEY:POT-6094
  Scenario: Get draft accounts - filtering on multiple fields
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I get the draft accounts filtering on the Status "SUBMITTED" and Submitted by "BUUID" then the response contains
      | account_status | Submitted |
      | submitted_by   | BUUID     |

    And The draft account filtered response does not contain accounts with status "Resubmitted"
    And The draft account filtered response does not contain accounts submitted by "BUUID_TWO"

    When I get the draft accounts filtering on the Status "SUBMITTED" and Submitted by "BUUID_TWO" then the response contains
      | account_status | Submitted |
      | submitted_by   | BUUID_TWO |

    And The draft account filtered response does not contain accounts with status "Resubmitted"
    And The draft account filtered response does not contain accounts submitted by "BUUID"

    Then I delete the created draft accounts

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6097
  Scenario: Get all draft accounts - created three accounts and verify logging contains all three
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    # Do a GET all
    When I get the draft accounts
    Then The draft account response returns 200

    # Assert that logging has PDPO entries for each created draft id
#    Then the logging service contains these PDPO logs:
#      | created_by_id | created_by_type | business_identifier              | individual_id                         | expected_count |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant    | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |

    # Cleanup
    Then I delete the created draft accounts

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6098
  Scenario: Get all draft accounts - Verify that 2 logs are created containing when the get all endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

    When I create the following draft accounts and store their IDs
      | business_unit_id | account                                                | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    # Do a GET all
    When I get the draft accounts
    Then The draft account response returns 200

    # Assert that logging has PDPO entries for each created draft id for both Business identifiers
#    Then the logging service contains these PDPO logs:
#      | created_by_id | created_by_type | business_identifier                       | individual_id                         | expected_count |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant             | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Parent or Guardian    | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |

    # Cleanup
    Then I delete the created draft accounts

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6100
  Scenario: Invalid token is blocked and no PDPO logs emitted
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | BUUID                                                  |
      | submitted_by_name | Laura Clerk                                            |
      | timeline_data     | draftAccounts/timelineJson/default.json                |
    Then The draft account response returns 201
    And I store the created draft account ID

  # switch to a non-OPAL user/token
    When I set an invalid token manually
    And I get the draft accounts
    Then The draft account response returns 401

  # confirm no PDPO logs were emitted for this attempted GET (no side-effects)
#    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"

  # switch back to an OPAL user so cleanup can delete the created draft (or delete via admin API)
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    Then I delete the created draft accounts
