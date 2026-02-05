@Opal
Feature: PO-745 patch draft account

  @PO-745 @PO-991 @cleanUpData
  Scenario: Patch draft account - Pending - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH001                                      |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id   | 73                          |
      | account_status     | Publishing Pending          |
      | validated_by       | PATCH001_REVIEWER              |
      | If-Match           | 0                           |

    Then The draft account response returns 200
    And the response must include a strong quoted ETag header
    And the response body must not include the "version" field anywhere

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Published            |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | PATCH001                |
      | account_snapshot.business_unit_name | West London          |
      | timeline_data[0].status             | Publishing Pending   |
      | timeline_data[0].username           | PATCH001_REVIEWER       |

    And the logging service contains these PDPO logs:
      | created_by_id   | created_by_type | business_identifier                          | expected_count |
      | PATCH001           | OPAL_USER_ID    | Re-submit Draft Account - Defendant         | 1              |


    Then I delete the created defendant accounts
    Then I delete the created draft accounts


  @PO-745 @cleanUpData
  Scenario: Patch draft account - Rejected - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH002                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | PATCH002_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 200

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                    |
      | account_type                        | Fine                  |
      | account_status                      | Rejected              |
      | account_snapshot.defendant_name     | LNAME, FNAME          |
      | account_snapshot.date_of_birth      | 2000-01-01            |
      | account_snapshot.account_type       | Fine                  |
      | account_snapshot.submitted_by       | PATCH002                 |
      | account_snapshot.business_unit_name | West London           |
      | timeline_data[0].status             | Rejected              |
      | timeline_data[0].username           | PATCH002_REVIEWER        |
      | timeline_data[0].reason_text        | Reason for rejection  |

    Then I delete the created draft accounts

  @PO-745 @cleanUpData
  Scenario: Patch draft account - Deleted - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                  |
      | account_status   | Deleted             |
      | validated_by     | BUUID_REVIEWER      |
      | reason_text      | Reason for deletion |
      | If-Match         | 0                   |
    Then The draft account response returns 200

    Then I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Deleted              |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | BUUID                |
      | account_snapshot.business_unit_name | West London          |
      | timeline_data[0].status             | Deleted              |
      | timeline_data[0].username           | BUUID_REVIEWER       |
      | timeline_data[0].reason_text        | Reason for deletion  |

    Then I delete the created draft accounts


  @PO-745 @PO-991 @cleanUpData
  Scenario: Patch draft account - Parent or Guardian - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/parentOrGuardianAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH003                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Publishing Pending   |
      | validated_by     | PATCH003               |
      | If-Match         | 0                    |

    Then The draft account response returns 200

    And the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                            | expected_count |
      | PATCH003         | OPAL_USER_ID    | Re-submit Draft Account - Parent or Guardian  | 1              |

    Then I delete the created draft accounts

  @PO-745 @PO-991 @cleanUpData
  Scenario: Patch draft account - Minor Creditor - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH004                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Publishing Pending   |
      | validated_by     | PATCH004                |
      | If-Match         | 0                    |

    Then The draft account response returns 200

    And the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                          | expected_count |
      | PATCH004         | OPAL_USER_ID    | Re-submit Draft Account - Minor Creditor    | 1              |

    Then I delete the created draft accounts

  @PO-745 @PO-991 @cleanUpData
  Scenario: Patch draft account - Defendant + Minor Creditor creates two logs
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/minorCreditorAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH005                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Publishing Pending   |
      | validated_by     | PATCH005                |
      | If-Match         | 0                    |

    Then The draft account response returns 200

    And the logging service contains these PDPO logs:
      | created_by_id | created_by_type | business_identifier                          | expected_count |
      | PATCH005         | OPAL_USER_ID    | Re-submit Draft Account - Defendant         | 1              |
      | PATCH005         | OPAL_USER_ID    | Re-submit Draft Account - Minor Creditor    | 1              |

    Then I delete the created draft accounts

  @PO-745 @cleanUpData
  Scenario: Attempt to patch with invalid token - no logs created
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | PATCH006                                      |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I attempt to update the draft account with an invalid token
    Then The draft account response returns 401

    Then no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Re-submit Draft Account - Defendant"

    Then I delete the created draft accounts


  @Opal
  @PO-745
  @cleanUpData
  Scenario: E2E.04 - Patch unknown draft account id returns 404 and does not create PDPO log
    Given I am testing as the "opal-test@hmcts.net" user

    When I patch the "00000000-0000-0000-0000-000000000000" draft account with the following details
      | business_unit_id | 73                 |
      | account_status   | Publishing Pending |
      | validated_by     | PATCH007     |
      | If-Match         | 0                  |
    Then The draft account response returns 406
    Then no PDPO logs exist for created_by id "PATCH007", type "OPAL_USER_ID" and business_identifier "Re-submit Draft Account - Defendant"




