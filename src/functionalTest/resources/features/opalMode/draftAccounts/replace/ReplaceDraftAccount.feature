@Opal @JIRA-LABEL:manual-account-creation
Feature: Replace Draft Account

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-746 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - update account details
    And a replaceable draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | If-Match          | 0                                           |

    Then the created draft account is replaced successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Resubmitted  |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |
      | account.originator_type             | TFO          |

    Then the original creation timestamp is preserved
    And I see the account status date is now after the initial account status date

  @JIRA-STORY:PO-947 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - update account details ignores submitted by name
    And a replaceable draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    When I update the draft account that was just created with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   | Submitted                                   |
      | submitted_by     | BUUID                                       |
      | timeline_data    | draftAccounts/timelineJson/default.json     |
      | If-Match         | 0                                           |

    Then the created draft account is replaced successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Resubmitted  |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |

    Then the original creation timestamp is preserved
    And I see the account status date is now after the initial account status date

  @JIRA-STORY:PO-2359 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355
  Scenario: Update draft account - Update (Defendant) logs PDPO
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | UPDATE001                                   |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | UPDATE001                                   |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | If-Match          | 0                                           |
    Then the request succeeds

    #    NOTE: This is temporarily commented out as the PDPO logging currently causes the log file to grow significantly when running the tests, which is causing issues in CI. Once we have a solution in place to prevent the log file from growing too much, we can uncomment this and verify the PDPO logs are being created as expected.
    #    And the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                       | individual_id                | expected_count |
    #      | 500000000        | OPAL_USER_ID    | Update Draft Account - Defendant          | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |

  @JIRA-STORY:PO-2359 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355
  Scenario: Update draft account - Parent + MinorCreditor yields two PDPO logs
    And a draft account exists with the following details
      | business_unit_id  | 73                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | UPDATE002                                           |
      | submitted_by_name | Laura Clerk                                         |
      | timeline_data     | draftAccounts/timelineJson/default.json             |

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | UPDATE002                                           |
      | submitted_by_name | Laura Clerk                                         |
      | timeline_data     | draftAccounts/timelineJson/default.json             |
      | If-Match          | 0                                                   |
    Then the request succeeds

    #    NOTE: This is temporarily commented out as the PDPO logging currently causes the log file to grow significantly when running the tests, which is causing issues in CI. Once we have a solution in place to prevent the log file from growing too much, we can uncomment this and verify the PDPO logs are being created as expected.
    #    And the logging service contains these PDPO logs:
    #      | created_by_id | created_by_type | business_identifier                          | individual_id                | expected_count |
    #      | 500000000     | OPAL_USER_ID    | Update Draft Account - Defendant             | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |
    #      | 500000000     | OPAL_USER_ID    | Update Draft Account - Minor Creditor        | <CREATED_DRAFT_ACCOUNT_ID>   | 1              |
