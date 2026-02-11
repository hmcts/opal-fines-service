@Opal
Feature: PO-746 put/update draft account

  @PO-746 @cleanUpData
  Scenario: Update draft account - update account details
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
    Then The draft account response returns 201
    And I store the created draft account ID
    And I store the created draft account created_at time
    And I store the created draft account initial account_status_date

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | BUUID       |
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

    Then The draft account response returns 200


    And I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Resubmitted          |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | BUUID                |
      | account_snapshot.business_unit_name | West London          |

    Then I see the created at time hasn't changed
    And I see the account status date is now after the initial account status date

    Then I delete the created draft accounts

  @PO-947 @cleanUpData
  Scenario: Update draft account - update account details submitted by name is required
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 201
    And I store the created draft account ID
    And I store the created draft account created_at time
    And I store the created draft account initial account_status_date

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | BUUID       |
      | account_snapshot.business_unit_name | West London |

    When I update the draft account that was just created with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   | Submitted                                   |
      | submitted_by     | BUUID                                       |
      | timeline_data    | draftAccounts/timelineJson/default.json     |
      | If-Match         | 0                                           |

    Then The draft account response returns 400

    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | BUUID       |
      | account_snapshot.business_unit_name | West London |

    Then I see the created at time hasn't changed
    And I see the account status date hasn't changed

    Then I delete the created draft accounts

  @PO-2359 @cleanUpData
  Scenario: Update draft account - Update (Defendant) logs PDPO
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | UPDATE001                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | UPDATE001                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | If-Match          | 0                                           |
    Then The draft account response returns 200

    And the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                       | expected_count |
      | UPDATE001         | OPAL_USER_ID    | Update Draft Account - Defendant         | 1              |

    Then I delete the created draft accounts



  @PO-2359 @cleanUpData
  Scenario: Update draft account - Parent + MinorCreditor yields two PDPO logs
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                                    |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json   |
      | account_type      | Fine                                                  |
      | account_status    | Submitted                                             |
      | submitted_by      | UPDATE002                                                 |
      | submitted_by_name | Laura Clerk                                           |
      | timeline_data     | draftAccounts/timelineJson/default.json               |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                                    |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json   |
      | account_type      | Fine                                                  |
      | account_status    | Submitted                                             |
      | submitted_by      | UPDATE002                                                 |
      | submitted_by_name | Laura Clerk                                           |
      | timeline_data     | draftAccounts/timelineJson/default.json               |
      | If-Match          | 0                                                     |
    Then The draft account response returns 200

    And the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                          | expected_count |
      | UPDATE002         | OPAL_USER_ID    | Update Draft Account - Defendant             | 1              |
      | UPDATE002         | OPAL_USER_ID    | Update Draft Account - Minor Creditor        | 1              |

    Then I delete the created draft accounts
