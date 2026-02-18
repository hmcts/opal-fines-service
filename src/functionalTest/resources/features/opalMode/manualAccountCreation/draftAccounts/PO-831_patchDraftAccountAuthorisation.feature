@Opal
Feature: PO-831 - Authorisation for patch draft account

  @PO-831 @cleanUpData
  Scenario: Patch draft account - no auth
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | If-Match          | 0                                      |


    Then The draft account response returns 201
    And I store the created draft account ID

    When I set an invalid token
    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 401

    Then I am testing as the "opal-test@hmcts.net" user


  @PO-831 @cleanUpData
  Scenario: Update draft account - user with no permissions
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | If-Match          | 0                                      |


    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test-2@hmcts.net" user
    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 403

    When I am testing as the "opal-test@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    Then I delete the created draft accounts

  @PO-831 @cleanUpData
  Scenario: Update draft account - user with permissions in different business unit - bu 73 to 26
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | If-Match          | 0                                      |

    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test-3@hmcts.net" user
    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 403

    When I am testing as the "opal-test@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    Then I delete the created draft accounts

  @PO-831 @cleanUpData
  Scenario: Update draft account - user with permissions in different business unit - bu 26 to 73
    Given I am testing as the "opal-test-3@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 26                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | version           | 0                                      |
    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test@hmcts.net" user
    And I patch the draft account with the following details
      | business_unit_id | 26                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 403

    When I am testing as the "opal-test-3@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 26            |
      | account_type                        | Fine          |
      | account_status                      | Submitted     |
      | account_snapshot.defendant_name     | null, null    |
      | account_snapshot.date_of_birth      |               |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.business_unit_name | Hertfordshire |

    Then I delete the created draft accounts

  @PO-831 @cleanUpData
  Scenario: Update draft account - user with permissions in same business unit
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    And I store the created draft account ID
    Then The draft account response returns 201

    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then The draft account response returns 200

    And I get the single created draft account and the response contains
      | business_unit_id                    | 73                   |
      | account_type                        | Fine                 |
      | account_status                      | Rejected             |
      | account_snapshot.defendant_name     | LNAME, FNAME         |
      | account_snapshot.date_of_birth      | 2000-01-01           |
      | account_snapshot.account_type       | Fine                 |
      | account_snapshot.submitted_by       | L073JG               |
      | account_snapshot.business_unit_name | West London          |

    Then I delete the created draft accounts

  @PO-831 @cleanUpData
  Scenario: Update draft account - user with permissions in same business unit - updating business unit
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
    And I store the created draft account ID
    Then The draft account response returns 201

    And I patch the draft account with the following details
      | business_unit_id | 77                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |
    Then The draft account response returns 409

    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    Then I delete the created draft accounts
