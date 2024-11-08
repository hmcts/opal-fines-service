@Opal
Feature: PO-829 Authorization for Get Draft Accounts

  @PO-829 @cleanUpData
  Scenario: Get Draft Accounts - No Permission
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 78                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    #the test user2 doesn't have permission to business unit id 78
    Given I am testing as the "opal-test-2@hmcts.net" user
    Then I get the draft accounts filtering on the Business unit "78" then the response contains
      | business_unit_id                    |  |
      | account_snapshot.business_unit_name |  |

    Then The draft account response returns 403
    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts

  @PO-829 @cleanUpData
  Scenario: Get Draft Accounts - No Permission - get draft accounts by business unit id/submitted by/status
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 65                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    Given I am testing as the "opal-test-4@hmcts.net" user
    When I get the draft accounts filtering on the Business unit "65" then the response contains
      | business_unit_id                    |  |
      | account_snapshot.business_unit_name |  |

    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts

  @PO-829 @cleanUpData
  Scenario: Get Draft Accounts - No Permission - No filters
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 65                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    # test user4 can check and validate draft account - AC4c. Request 3
    Given I am testing as the "opal-test-4@hmcts.net" user

    When I get the draft accounts filtering on the Business unit "65" then the response contains
      | business_unit_id                    | 65                   |
      | account_snapshot.business_unit_name | Camden and Islington |

    When I get the draft accounts filtering on the Status "SUBMITTED" then the response contains
      | account_status | Submitted |
    And The draft account filtered response does not contain accounts with status "Resubmitted"


    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts

  @PO-829 @cleanUpData
  Scenario: Get Draft Accounts - No Permission - No filters
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 65                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    # test user3 has create and manage draft account - AC4c. Request 2
    Given I am testing as the "opal-test-3@hmcts.net" user

    When I get the draft accounts filtering on the Business unit "65" then the response contains
      | business_unit_id                    | 65                   |
      | account_snapshot.business_unit_name | Camden and Islington |

    When I get the draft accounts filtering on the Status "SUBMITTED" then the response contains
      | account_status | rejected |
    And The draft account filtered response does not contain accounts with status "Resubmitted"


    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts

  @PO-829 @cleanUpData
  Scenario: Get Draft Accounts - No Permission
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 66                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    # test user3 has create and manage draft account - AC4c. Request 1


    When I get the draft accounts filtering on the draft account id then the response contains
      | business_unit_id                    | 65                   |
      | account_snapshot.business_unit_name | Camden and Islington |

    When I get the draft accounts filtering on the Status "SUBMITTED" then the response contains
      | account_status | rejected |
    And The draft account filtered response does not contain accounts with status "Resubmitted"


    Given I am testing as the "opal-test@hmcts.net" user

    Then I delete the created draft accounts


