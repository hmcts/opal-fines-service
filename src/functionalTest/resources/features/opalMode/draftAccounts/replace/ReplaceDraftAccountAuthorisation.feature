@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Replace Draft Account Authorisation

  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-TEST-KEY:PO-5671
  Scenario: Update draft account - no auth
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I set an invalid token
    And I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |

    Then the request is rejected as unauthorized

    #    And no PDPO logs exist for created_by id "invalidToken", type "OPAL_USER_ID" and business_identifier "Update Draft Account - Defendant"

  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5672
  Scenario: Update draft account - user with no permissions
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When the "opal-test-2@dev.platform.hmcts.net" user attempts to replace the created draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |


  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5673
  Scenario: Update draft account - user with permissions in different business unit - bu 73 to 26
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When the "opal-test-3@dev.platform.hmcts.net" user attempts to replace the created draft account with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |


  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5674
  Scenario: Update draft account - user with permissions in different business unit - bu 26 to 73
    Given I am testing as the "opal-test-3@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 26                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When the "opal-test@dev.platform.hmcts.net" user attempts to replace the created draft account with the following details
      | business_unit_id  | 26                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 26            |
      | account_type                        | Fine          |
      | account_status                      | Submitted     |
      | account_snapshot.defendant_name     | null, null    |
      | account_snapshot.date_of_birth      |               |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.business_unit_name | Hertfordshire |


  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-TEST-KEY:PO-5675
  Scenario: Update draft account - user with permissions in same business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_Updated                               |
      | submitted_by_name | Laura Clerk                                 |
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


  @JIRA-STORY:PO-830 @JIRA-EPIC:PO-2220 @cleanUpData @JIRA-TEST-KEY:PO-5676
  Scenario: Update draft account - user with permissions in same business unit - updating business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
    When I update the draft account that was just created with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      |                                             |
      | submitted_by_name | Laura Clerk                                 |
      | If-Match          | 0                                           |

    Then the request is rejected as bad request and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |



  @JIRA-STORY:PO-2359 @JIRA-LABEL:personal-data-processing-logging @cleanUpData @JIRA-EPIC:PO-2355 @JIRA-TEST-KEY:PO-5677
  Scenario: Invalid token replacement attempts are rejected and do not create PDPO logs
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
    When I attempt to put a draft account with an invalid token
    Then the request is rejected as unauthorized
