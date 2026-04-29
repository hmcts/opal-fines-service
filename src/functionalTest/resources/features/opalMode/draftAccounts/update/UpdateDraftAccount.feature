@Opal @JIRA-LABEL:manual-account-creation
Feature: Update Draft Accounts

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-745 @JIRA-STORY:PO-991 @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2220 @JIRA-KEY:POT-6144
  Scenario: Reject publishing a submitted draft account
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH001                                    |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I patch the draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH001_REVIEWER  |
      | If-Match         | 0                  |

    Then the request is rejected as forbidden

  @JIRA-STORY:PO-745 @cleanUpData @JIRA-EPIC:PO-2220 @JIRA-KEY:POT-6146
  Scenario: Mark a submitted draft account as rejected
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH002                                    |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | PATCH002_REVIEWER    |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the created draft account is patched successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Rejected             |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |
      | timeline_data[0].status             | Rejected             |
      | timeline_data[0].username           | PATCH002_REVIEWER    |
      | timeline_data[0].reason_text        | Reason for rejection |

  @JIRA-STORY:PO-745 @cleanUpData @JIRA-EPIC:PO-2220 @JIRA-KEY:POT-6147
  Scenario: Mark a submitted draft account as deleted
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I patch the draft account with the following details
      | business_unit_id | 73                  |
      | account_status   | Deleted             |
      | validated_by     | BUUID_REVIEWER      |
      | reason_text      | Reason for deletion |
      | If-Match         | 0                   |
    Then the created draft account is patched successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73                  |
      | account_type                        | Fine                |
      | account_status                      | Deleted             |
      | account_snapshot.defendant_name     | LNAME, FNAME        |
      | account_snapshot.date_of_birth      | 2000-01-01          |
      | account_snapshot.account_type       | Fine                |
      | account_snapshot.submitted_by       | L073JG              |
      | account_snapshot.business_unit_name | West London         |
      | timeline_data[0].status             | Deleted             |
      | timeline_data[0].username           | BUUID_REVIEWER      |
      | timeline_data[0].reason_text        | Reason for deletion |

  @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-KEY:POT-6149
  Scenario: Reject publishing a parent or guardian draft account
    And a draft account exists with the following details
      | business_unit_id  | 73                                                     |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                                   |
      | account_status    | Submitted                                              |
      | submitted_by      | PATCH003                                               |
      | submitted_by_name | Laura Clerk                                            |
      | timeline_data     | draftAccounts/timelineJson/default.json                |

    When I patch the draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH003           |
      | If-Match         | 0                  |

    Then the request is rejected as forbidden

  @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-KEY:POT-6151
  Scenario: Reject publishing a minor creditor draft account
    And a draft account exists with the following details
      | business_unit_id  | 73                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | PATCH004                                            |
      | submitted_by_name | Laura Clerk                                         |
      | timeline_data     | draftAccounts/timelineJson/default.json             |

    When I patch the draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH004           |
      | If-Match         | 0                  |

    Then the request is rejected as forbidden

  @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-KEY:POT-6153
  Scenario: Reject publishing a minor creditor draft account with a defendant
    And a draft account exists with the following details
      | business_unit_id  | 73                                                  |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                                |
      | account_status    | Submitted                                           |
      | submitted_by      | PATCH005                                            |
      | submitted_by_name | Laura Clerk                                         |
      | timeline_data     | draftAccounts/timelineJson/default.json             |

    When I patch the draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH005           |
      | If-Match         | 0                  |

    Then the request is rejected as forbidden

  @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-KEY:POT-6155
  Scenario: Reject updating a draft account with an invalid token
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH006                                    |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I attempt to update the draft account with an invalid token
    Then the request is rejected as unauthorized

    #    NOTE: This is temporarily commented out as the PDPO logging currently causes the log file to grow significantly when running the tests, which is causing issues in CI. Once we have a solution in place to prevent the log file from growing too much, we can uncomment this and verify the PDPO logs are being created as expected.
    #    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Re-submit Draft Account - Defendant"

  @JIRA-STORY:PO-2358 @JIRA-LABEL:personal-data-processing-logging @JIRA-EPIC:PO-2355 @cleanUpData @JIRA-KEY:POT-6156
  Scenario: Reject updating an unknown draft account id

    When I patch the "00000000-0000-0000-0000-000000000000" draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH007           |
      | If-Match         | 0                  |
    Then the request is rejected as not acceptable

#    NOTE: This is temporarily commented out as the PDPO logging currently causes the log file to grow significantly when running the tests, which is causing issues in CI. Once we have a solution in place to prevent the log file from growing too much, we can uncomment this and verify the PDPO logs are being created as expected.
#    Then no PDPO logs exist for created_by id "PATCH007", type "OPAL_USER_ID" and business_identifier "Re-submit Draft Account - Defendant"
