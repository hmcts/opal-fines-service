@Opal
Feature: PO-606 get draft accounts

  @PO-606 @cleanUpData @JIRA-KEY:POT-207
  Scenario: Get draft accounts - filtering on business unit
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

    When I create a draft account with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I create a draft account with the following details
      | business_unit_id  | 65                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I get the draft accounts filtering on the Business unit "73" then the response contains
      | business_unit_id                    | 73          |
      | account_snapshot.business_unit_name | West London |
    And The draft account filtered response does not contain accounts in the "77" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    When I get the draft accounts filtering on the Business unit "77" then the response contains
      | business_unit_id                    | 77               |
      | account_snapshot.business_unit_name | Camberwell Green |
    And The draft account filtered response does not contain accounts in the "73" business unit
    And The draft account filtered response does not contain accounts in the "65" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    When I get the draft accounts filtering on the Business unit "65" then the response contains
      | business_unit_id                    | 65                   |
      | account_snapshot.business_unit_name | Camden and Islington |
    And The draft account filtered response does not contain accounts in the "73" business unit
    And The draft account filtered response does not contain accounts in the "77" business unit
    Then The draft account response returns 200
    And the response body must not include the "version" field anywhere

    Then I delete the created draft accounts

  @PO-606 @cleanUpData @JIRA-KEY:POT-208
  Scenario: Get draft accounts - filtering on status
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

    When I update the draft account that was just created with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |
      | If-Match          | 0                                           |

    When I create a draft account with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I create a draft account with the following details
      | business_unit_id  | 65                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I get the draft accounts filtering on the Status "SUBMITTED" then the response contains
      | account_status | Submitted |
    And The draft account filtered response does not contain accounts with status "Resubmitted"

    When I get the draft accounts filtering on the Status "RESUBMITTED" then the response contains
      | account_status | Resubmitted |
    And The draft account filtered response does not contain accounts with status "Submitted"

    Then I delete the created draft accounts

  @PO-606 @cleanUpData @JIRA-KEY:POT-209
  Scenario: Get draft accounts - filtering on submitted_by
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

    When I create a draft account with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I create a draft account with the following details
      | business_unit_id  | 65                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_TWO                                   |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I get the draft accounts filtering on Submitted by "BUUID" then the response contains
      | account_snapshot.submitted_by | BUUID |
    And The draft account filtered response does not contain accounts submitted by "BUUID_TWO"

    When I get the draft accounts filtering on Submitted by "BUUID_TWO" then the response contains
      | account_snapshot.submitted_by | BUUID_TWO |
    And The draft account filtered response does not contain accounts submitted by "BUUID"

    Then I delete the created draft accounts

  @PO-606 @cleanUpData @JIRA-KEY:POT-210
  Scenario: Get draft accounts - filtering on multiple fields
    Given I am testing as the "opal-test@HMCTS.net" user
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

    When I create a draft account with the following details
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I create a draft account with the following details
      | business_unit_id  | 65                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID_TWO                                   |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    Then The draft account response returns 201
    And I store the created draft account ID

    When I get the draft accounts filtering on the Status "SUBMITTED" and Submitted by "BUUID" then the response contains
      | account_status   | Submitted   |
      | submitted_by     | BUUID       |

    And The draft account filtered response does not contain accounts with status "Resubmitted"
    And The draft account filtered response does not contain accounts submitted by "BUUID_TWO"

    When I get the draft accounts filtering on the Status "SUBMITTED" and Submitted by "BUUID_TWO" then the response contains
      | account_status   | Submitted   |
      | submitted_by     | BUUID_TWO   |

    And The draft account filtered response does not contain accounts with status "Resubmitted"
    And The draft account filtered response does not contain accounts submitted by "BUUID"

    Then I delete the created draft accounts
