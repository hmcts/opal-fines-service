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
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id   | 73                          |
      | account_status     | Publishing Pending          |
      | validated_by       | BUUID_REVIEWER              |
      | If-Match           | 0                           |

    Then The draft account response returns 403

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
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
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
      | account_snapshot.submitted_by       | L073JG                |
      | account_snapshot.business_unit_name | West London           |
      | timeline_data[0].status             | Rejected              |
      | timeline_data[0].username           | BUUID_REVIEWER        |
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
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |
      | timeline_data[0].status             | Deleted              |
      | timeline_data[0].username           | BUUID_REVIEWER       |
      | timeline_data[0].reason_text        | Reason for deletion  |

    Then I delete the created draft accounts
