@Opal
Feature: PO-830 - Authorisation for put/update draft account

  @PO-830 @cleanUpData
  Scenario: Update draft account - no auth
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

    When I set an invalid token
    And I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 401

    Then I am testing as the "opal-test@hmcts.net" user

  @PO-830 @cleanUpData
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
    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test-2@hmcts.net" user
    And I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 403

    When I am testing as the "opal-test@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | BUUID       |
      | account_snapshot.business_unit_name | West London |

    Then I delete the created draft accounts

  @PO-830 @cleanUpData
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
    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test-3@hmcts.net" user
    And I update the draft account that was just created with the following details      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 403

    When I am testing as the "opal-test@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | BUUID       |
      | account_snapshot.business_unit_name | West London |

    Then I delete the created draft accounts

  @PO-830 @cleanUpData
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
    And I store the created draft account ID
    Then The draft account response returns 201

    When I am testing as the "opal-test@hmcts.net" user
    And I update the draft account that was just created with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 403

    When I am testing as the "opal-test-3@hmcts.net" user
    And I get the single created draft account and the response contains
      | business_unit_id                    | 26            |
      | account_type                        | Fine          |
      | account_status                      | Submitted     |
      | account_snapshot.defendant_name     | null, null    |
      | account_snapshot.date_of_birth      |               |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.submitted_by       | BUUID         |
      | account_snapshot.business_unit_name | Hertfordshire |

    Then I delete the created draft accounts

  @PO-830 @cleanUpData
  Scenario: Update draft account - user with permissions in same business unit
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

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | version           | 0                                           |

    Then The draft account response returns 200

    And I get the single created draft account and the response contains
      | business_unit_id                    | 73            |
      | account_type                        | Fine          |
      | account_status                      | Resubmitted   |
      | account_snapshot.defendant_name     | LNAME, FNAME  |
      | account_snapshot.date_of_birth      | 01/01/2000    |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.submitted_by       | BUUID_Updated |
      | account_snapshot.business_unit_name | West London   |

    Then I delete the created draft accounts

    @PO-830 @cleanUpData
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

      When I update the draft account that was just created with the following details
        | business_unit_id  | 77                                          |
        | account           | draftAccounts/accountJson/adultAccount.json |
        | account_type      | Fine                                        |
        | account_status    | Submitted                                   |
        | submitted_by      |                                             |
        | submitted_by_name | Laura Clerk                                 |
        | timeline_data     | draftAccounts/timelineJson/default.json     |

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

      Then I delete the created draft accounts
