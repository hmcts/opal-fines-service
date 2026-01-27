@Opal
Feature: PO-749 put draft account error handling


  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP1 - Invalid Request Payload
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

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |

    When I attempt to put a draft account with an invalid request payload
      | business_unit_id  |                                        |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 400
    Then I delete the created draft accounts

  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP2 - Invalid or No Access Token
    Given I am testing as the "opal-test@hmcts.net" user
    When I attempt to put a draft account with an invalid token
    Then The draft account response returns 401

  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP4 - Resource Not Found
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | version           | 0                                      |

    Then The draft account response returns 201
    And I store the created draft account ID
    And I store the created draft account created_at time

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |
    When I attempt to put a draft account with resource not found
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
      | If-Match          | 0                                      |

    Then The draft account response returns 404
    Then I delete the created draft accounts

  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP5 - Unsupported Content Type for Response
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

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |
    When I attempt to put a draft account with unsupported content type for response
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 406
    Then I delete the created draft accounts

  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP7 - Unsupported Media Type for Request
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

    And The draft account response contains the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |
    When I attempt to put a draft account with unsupported media type for request
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    | Submitted                              |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|

    Then The draft account response returns 406
    Then I delete the created draft accounts

  @PO-749 @cleanUpData
  Scenario: Put draft account - CEP9 - Other Server Error
    Given I am testing as the "opal-test@hmcts.net" user
    When I put the draft account trying to provoke an internal server error
    Then The draft account response returns 500
