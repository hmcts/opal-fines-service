@Opal @JIRA-LABEL:manual-account-creation @R1A
Feature: List Draft Accounts

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by business unit <business_unit>
    And the following draft accounts exist
      | source | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk

    When I request draft accounts for business unit "<business_unit>"
    Then only draft accounts for business unit "<business_unit>" are returned
    And the returned draft accounts identify business unit as "<business_unit_name>"
    And the returned draft accounts exclude business units "<excluded_units>"
    And the response body must not include the "version" field anywhere

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5650
    Examples:
      | business_unit | business_unit_name   | excluded_units |
      | 73            | West London          | 77, 65         |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5651
    Examples:
      | business_unit | business_unit_name | excluded_units |
      | 77            | Camberwell Green   | 73, 65         |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5652
    Examples:
      | business_unit | business_unit_name   | excluded_units |
      | 65            | Camden and Islington | 73, 77         |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by status <status_filter>
    Given a resubmitted draft account exists with the following details
      | source            | MAC                                         |
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    And the following draft accounts exist
      | source | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk

    When I request draft accounts for status "<status_filter>"
    Then only draft accounts with status "<expected_status>" are returned
    And the returned draft accounts exclude status "<excluded_status>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5653
    Examples:
      | status_filter | expected_status | excluded_status |
      | SUBMITTED     | Submitted       | Resubmitted     |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5654
    Examples:
      | status_filter | expected_status | excluded_status |
      | RESUBMITTED   | Resubmitted     | Submitted       |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by submitted_by <submitted_by_filter>
    And the following draft accounts exist
      | source | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk

    When I request draft accounts submitted by "<submitted_by_filter>"
    Then only draft accounts submitted by "<expected_submitted_by>" are returned
    And the returned draft accounts exclude accounts submitted by "<excluded_submitted_by>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5655
    Examples:
      | submitted_by_filter | expected_submitted_by | excluded_submitted_by |
      | BUUID              | BUUID                 | BUUID_TWO             |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5656
    Examples:
      | submitted_by_filter | expected_submitted_by | excluded_submitted_by |
      | BUUID_TWO           | BUUID_TWO             | BUUID                 |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by status <status_filter> and submitted_by <submitted_by_filter>
    And the following draft accounts exist
      | source | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk

    When I request draft accounts for status "<status_filter>" and submitted by "<submitted_by_filter>"
    Then only draft accounts with status "<expected_status>" submitted by "<expected_submitted_by>" are returned
    And the returned draft accounts exclude status "Resubmitted"
    And the returned draft accounts exclude accounts submitted by "<excluded_submitted_by>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5657
    Examples:
      | status_filter | submitted_by_filter | expected_status | expected_submitted_by | excluded_submitted_by |
      | SUBMITTED     | BUUID              | Submitted       | BUUID                 | BUUID_TWO             |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-TEST-KEY:PO-5658
    Examples:
      | status_filter | submitted_by_filter | expected_status | expected_submitted_by | excluded_submitted_by |
      | SUBMITTED     | BUUID_TWO           | Submitted       | BUUID_TWO             | BUUID                 |

  @JIRA-STORY:PO-2361 @JIRA-EPIC:PO-2355 @cleanUpData @JIRA-TEST-KEY:PO-5659 @ExtendedTest
  Scenario: Retrieving all draft accounts produces defendant PDPO logs

    And the following draft accounts exist
      | source | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk

    When I request all draft accounts
    Then the request succeeds

    # Assert that logging has PDPO entries for each created draft id
    Then the logging service contains draft-account PDPO logs covering these operations:
      | created_by_id | created_by_type | business_identifier           |
      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant |

    # Cleanup

  @JIRA-STORY:PO-2361 @JIRA-EPIC:PO-2355 @cleanUpData @JIRA-TEST-KEY:PO-5660 @ExtendedTest
  Scenario: Retrieving all draft accounts with parent or guardian data produces two PDPO log categories

    And the following draft accounts exist
      | source | business_unit_id | account                                                | account_type | account_status | submitted_by | submitted_by_name
      | MAC    | 73               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 77               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk
      | MAC    | 65               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk

    When I request all draft accounts
    Then the request succeeds

    # Assert that logging has PDPO entries for each created draft id for both Business identifiers
    Then the logging service contains draft-account PDPO logs covering these operations:
      | created_by_id | created_by_type | business_identifier                    |
      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant          |
      | 500000000     | OPAL_USER_ID    | Get Draft Account - Parent or Guardian |

    # Cleanup

  @JIRA-STORY:PO-2361 @JIRA-EPIC:PO-2355 @JIRA-TEST-KEY:PO-5661
  Scenario: Invalid token is blocked when listing draft accounts
    When I attempt to get draft accounts with an invalid token
    Then the request is rejected as unauthorized

    # DraftAccountListSecurityIT verifies no PDPO publisher calls for rejected requests.
    # A valid list request elsewhere can log any draft on a shared environment, so even a
    # newly created draft ID cannot isolate this request in the logging search API.
