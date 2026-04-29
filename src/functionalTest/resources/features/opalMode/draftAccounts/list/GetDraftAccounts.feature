@Opal @JIRA-LABEL:manual-account-creation
Feature: List Draft Accounts

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by business unit <business_unit>
    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request draft accounts for business unit "<business_unit>"
    Then only draft accounts for business unit "<business_unit>" are returned
    And the returned draft accounts identify business unit as "<business_unit_name>"
    And the returned draft accounts exclude business units "<excluded_units>"
    And the response body must not include the "version" field anywhere

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-KEY:POT-6087
    Examples:
      | business_unit | business_unit_name   | excluded_units |
      | 73            | West London          | 77, 65         |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219
    Examples:
      | business_unit | business_unit_name | excluded_units |
      | 77            | Camberwell Green   | 73, 65         |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219
    Examples:
      | business_unit | business_unit_name   | excluded_units |
      | 65            | Camden and Islington | 73, 77         |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by status <status_filter>
    Given a resubmitted draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request draft accounts for status "<status_filter>"
    Then only draft accounts with status "<expected_status>" are returned
    And the returned draft accounts exclude status "<excluded_status>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-KEY:POT-6090
    Examples:
      | status_filter | expected_status | excluded_status |
      | SUBMITTED     | Submitted       | Resubmitted     |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219
    Examples:
      | status_filter | expected_status | excluded_status |
      | RESUBMITTED   | Resubmitted     | Submitted       |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by submitted_by <submitted_by_filter>
    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request draft accounts submitted by "<submitted_by_filter>"
    Then only draft accounts submitted by "<expected_submitted_by>" are returned
    And the returned draft accounts exclude accounts submitted by "<excluded_submitted_by>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-KEY:POT-6092
    Examples:
      | submitted_by_filter | expected_submitted_by | excluded_submitted_by |
      | BUUID              | BUUID                 | BUUID_TWO             |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219
    Examples:
      | submitted_by_filter | expected_submitted_by | excluded_submitted_by |
      | BUUID_TWO           | BUUID_TWO             | BUUID                 |

  @cleanUpData
  Scenario Outline: Draft accounts can be filtered by status <status_filter> and submitted_by <submitted_by_filter>
    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/account.json      | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID_TWO    | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request draft accounts for status "<status_filter>" and submitted by "<submitted_by_filter>"
    Then only draft accounts with status "<expected_status>" submitted by "<expected_submitted_by>" are returned
    And the returned draft accounts exclude status "Resubmitted"
    And the returned draft accounts exclude accounts submitted by "<excluded_submitted_by>"

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219 @JIRA-KEY:POT-6094
    Examples:
      | status_filter | submitted_by_filter | expected_status | expected_submitted_by | excluded_submitted_by |
      | SUBMITTED     | BUUID              | Submitted       | BUUID                 | BUUID_TWO             |

    @JIRA-STORY:PO-606 @JIRA-EPIC:PO-2219
    Examples:
      | status_filter | submitted_by_filter | expected_status | expected_submitted_by | excluded_submitted_by |
      | SUBMITTED     | BUUID_TWO           | Submitted       | BUUID_TWO             | BUUID                 |

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6097
  Scenario: Retrieving all draft accounts produces defendant PDPO logs

    And the following draft accounts exist
      | business_unit_id | account                                     | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/adultAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request all draft accounts
    Then the request succeeds

    # Assert that logging has PDPO entries for each created draft id
#    Then the logging service contains these PDPO logs:
#      | created_by_id | created_by_type | business_identifier              | individual_id                         | expected_count |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant    | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |

    # Cleanup

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6098
  Scenario: Retrieving all draft accounts with parent or guardian data produces two PDPO log categories

    And the following draft accounts exist
      | business_unit_id | account                                                | account_type | account_status | submitted_by | submitted_by_name | timeline_data                         |
      | 73               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 77               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |
      | 65               | draftAccounts/accountJson/parentOrGuardianAccount.json | Fine         | Submitted      | BUUID        | Laura Clerk      | draftAccounts/timelineJson/default.json |

    When I request all draft accounts
    Then the request succeeds

    # Assert that logging has PDPO entries for each created draft id for both Business identifiers
#    Then the logging service contains these PDPO logs:
#      | created_by_id | created_by_type | business_identifier                       | individual_id                         | expected_count |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Defendant             | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |
#      | 500000000     | OPAL_USER_ID    | Get Draft Account - Parent or Guardian    | <CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>| 1              |

    # Cleanup

  @PO-2361 @cleanUpData @JIRA-KEY:POT-6100
  Scenario: Invalid token is blocked and no PDPO logs emitted
    Given a draft account exists with the following details
      | business_unit_id  | 73                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | BUUID                                                  |
      | submitted_by_name | Laura Clerk                                            |
      | timeline_data     | draftAccounts/timelineJson/default.json                |

    When I attempt to get draft accounts with an invalid token
    Then the request is rejected as unauthorized

  # confirm no PDPO logs were emitted for this attempted GET (no side-effects)
  # Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Get Draft Account - Defendant"
