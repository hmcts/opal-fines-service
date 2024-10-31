@Opal
Feature: PO-828 Authorization for get draft account

  @PO-828 @cleanUpData
  Scenario: Authorization for Test User 1 to Check and Validate Draft Accounts
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I create a draft account with the following details
      | business_unit_id | 80                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    When I get the draft accounts filtering on the Business unit "73" then the response contains
      | business_unit_id                    | 73          |
      | account_snapshot.business_unit_name | West London |
    And The draft account filtered response does not contain accounts in the "77" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit

    When I get the draft accounts filtering on the Business unit "80" then the response contains
      | business_unit_id                    | 80               |
      | account_snapshot.business_unit_name | Historical Debt |
    And The draft account filtered response does not contain accounts in the "73" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit

    Then I delete the created draft accounts

  @PO-828 @cleanUpData
     #Test user 2 has no permissions to create or manage draft accounts
  Scenario: No Authorization for Test User 2 to Create or manage Draft Accounts
    Given I am testing as the "opal-test-2@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 26                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403

  @PO-828 @cleanUpData
  Scenario: Authorization for Test User 3 can Create or manage draft accounts with in BU
    Given I am testing as the "opal-test-3@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 26                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 26            |
      | account_type                        | Fine          |
      | account_status                      | Submitted     |
      | account_snapshot.defendant_name     | LNAME, FNAME  |
      | account_snapshot.date_of_birth      | 01/01/2000    |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.submitted_by       | BUUID         |
      | account_snapshot.business_unit_name | Hertfordshire |

    When I get the draft accounts filtering on the Business unit "26" then the response contains
      | business_unit_id                    | 26            |
      | account_snapshot.business_unit_name | Hertfordshire |

    Then I delete the created draft accounts

  @PO-828 @cleanUpData
  Scenario: No authorization for Test User 3 to Create or manage draft accounts other than specific BU
    Given I am testing as the "opal-test-3@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 77                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403

  @PO-828 @cleanUpData
  Scenario: Authorization for Test User 5 to Create or manage draft accounts
    Given I am testing as the "opal-test-5@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 60                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 60           |
      | account_type                        | Fine         |
      | account_status                      | Submitted    |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 01/01/2000   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | BUUID        |
      | account_snapshot.business_unit_name | Dyfed Powys  |

    When I get the draft accounts filtering on the Business unit "60" then the response contains
      | business_unit_id                    | 60          |
      | account_snapshot.business_unit_name | Dyfed Powys |

    Then I delete the created draft accounts

#  @PO-828 @cleanUpData
#  Scenario: Authorization for Test User 6 to Create or manage draft accounts
#    Given I am testing as the "opal-test-6@HMCTS.NET" user
#    When I create a draft account with the following details
#      | business_unit_id | 106                                         |
#      | account          | draftAccounts/accountJson/adultAccount.json |
#      | account_type     | Fine                                        |
#      | account_status   |                                             |
#      | submitted_by     | BUUID                                       |
#      | timeline_data    |                                             |
#    Then The draft account response returns 201
#    And I store the created draft account ID
#
#    And The draft account response contains the following data
#      | business_unit_id                    | 106          |
#      | account_type                        | Fine         |
#      | account_status                      | Submitted    |
#      | account_snapshot.defendant_name     | LNAME, FNAME |
#      | account_snapshot.date_of_birth      | 01/01/2000   |
#      | account_snapshot.account_type       | Fine         |
#      | account_snapshot.submitted_by       | BUUID        |
#      | account_snapshot.business_unit_name | North Wales  |
#
#    When I get the draft accounts filtering on the Business unit "106" then the response contains
#      | business_unit_id                    | 106         |
#      | account_snapshot.business_unit_name | North Wales |
#
#    Then I delete the created draft accounts

#  @PO-828 @cleanUpData
#  Scenario: Authorization for Test User 7 to Create or manage draft accounts
#    Given I am testing as the "opal-test-7@HMCTS.NET" user
#    When I create a draft account with the following details
#      | business_unit_id | 89                                          |
#      | account          | draftAccounts/accountJson/adultAccount.json |
#      | account_type     | Fine                                        |
#      | account_status   |                                             |
#      | submitted_by     | BUUID                                       |
#      | timeline_data    |                                             |
#    Then The draft account response returns 201
#    And I store the created draft account ID
#
#    And The draft account response contains the following data
#      | business_unit_id                    | 89           |
#      | account_type                        | Fine         |
#      | account_status                      | Submitted    |
#      | account_snapshot.defendant_name     | LNAME, FNAME |
#      | account_snapshot.date_of_birth      | 01/01/2000   |
#      | account_snapshot.account_type       | Fine         |
#      | account_snapshot.submitted_by       | BUUID        |
#      | account_snapshot.business_unit_name | Gwent        |
#
#    When I get the draft accounts filtering on the Business unit "89" then the response contains
#      | business_unit_id                    | 89    |
#      | account_snapshot.business_unit_name | Gwent |
#
#    Then I delete the created draft accounts

  @PO-828 @cleanUpData
  Scenario: Authorization for Test User 8 to Check and Validate Draft Accounts or Create or manage draft accounts
    Given I am testing as the "opal-test-8@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 36                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 36                           |
      | account_type                        | Fine                         |
      | account_status                      | Submitted                    |
      | account_snapshot.defendant_name     | LNAME, FNAME                 |
      | account_snapshot.date_of_birth      | 01/01/2000                   |
      | account_snapshot.account_type       | Fine                         |
      | account_snapshot.submitted_by       | BUUID                        |
      | account_snapshot.business_unit_name | LCIS (South + Mid Glamorgan) |

    When I get the draft accounts filtering on the Business unit "36" then the response contains
      | business_unit_id                    | 36                           |
      | account_snapshot.business_unit_name | LCIS (South + Mid Glamorgan) |

    Then I delete the created draft accounts

  @PO-828 @cleanUpData
  Scenario: No Authorization for Test User 8 to Check and Validate Draft Accounts draft accounts in other BU
    Given I am testing as the "opal-test-8@HMCTS.NET" user
    When I create a draft account with the following details
      | business_unit_id | 36                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    And The draft account response contains the following data
      | business_unit_id                    | 36                           |
      | account_type                        | Fine                         |
      | account_status                      | Submitted                    |
      | account_snapshot.defendant_name     | LNAME, FNAME                 |
      | account_snapshot.date_of_birth      | 01/01/2000                   |
      | account_snapshot.account_type       | Fine                         |
      | account_snapshot.submitted_by       | BUUID                        |
      | account_snapshot.business_unit_name | LCIS (South + Mid Glamorgan) |

    When I attempt to get the draft accounts filtering on the other Business unit "80" then the response is 404






