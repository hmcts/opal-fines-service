@Opal
Feature: PO-747 patch draft account error handling

  @PO-747 @cleanUpData
  Scenario: Patch draft account - CEP1 - Invalid Request Payload
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id  | 73                                     |
      | account           | draftAccounts/accountJson/account.json |
      | account_type      | Fine                                   |
      | account_status    |                                        |
      | submitted_by      | BUUID                                  |
      | submitted_by_name | Laura Clerk                            |
      | timeline_data     | draftAccounts/timelineJson/default.json|
    Then The draft account response returns 201
    And I store the created draft account ID

    When I patch the draft account with the following details
      | business_unit_id__ | 73                   |
      | account_status     | Rejected             |
      | validated_by       | BUUID_REVIEWER       |
      | reason_text        | Reason for rejection |
    Then The draft account response returns 400

    @PO-747 @cleanUpData
      Scenario: Patch draft account - CEP2 - Invalid or No Access Token
        Given I am testing as the "opal-test@hmcts.net" user
        When I create a draft account with the following details
          | business_unit_id  | 73                                     |
          | account           | draftAccounts/accountJson/account.json |
          | account_type      | Fine                                   |
          | account_status    |                                        |
          | submitted_by      | BUUID                                  |
          | submitted_by_name | Laura Clerk                            |
          | timeline_data     | draftAccounts/timelineJson/default.json|
        Then The draft account response returns 201
        And I store the created draft account ID

        When I set an invalid token
       And I patch the draft account with the following details
          | business_unit_id | 73                   |
          | account_status   | Rejected             |
          | validated_by     | BUUID_REVIEWER       |
          | reason_text      | Reason for rejection |
        Then The draft account response returns 401

      Then I am testing as the "opal-test@hmcts.net" user

      @PO-747 @cleanUpData
      Scenario: Patch draft account - CEP4 - Resource Not Found
        Given I am testing as the "opal-test@hmcts.net" user
        When I patch the "1000000000" draft account with the following details
          | business_unit_id | 73                   |
          | account_status   | Rejected             |
          | validated_by     | BUUID_REVIEWER       |
          | reason_text      | Reason for rejection |
        Then The draft account response returns 404

#      @PO-747 @cleanUpData
#      Scenario: Patch draft account - CEP5 - Unsupported Content Type
#        Given I am testing as the "opal-test@hmcts.net" user
#        When I attempt to patch a draft account with an unsupported content type
#        Then The draft account response returns 406

      @PO-747 @cleanUpData
      Scenario: Patch draft account - CEP7 - Unsupported Media Type
        Given I am testing as the "opal-test@hmcts.net" user
        When I attempt to patch a draft account with an unsupported media type
        Then The draft account response returns 415

      @PO-747 @cleanUpData
        Scenario: Patch draft account - CEP9 - Other Server Error
          Given I am testing as the "opal-test@hmcts.net" user
          When I patch the draft account trying to provoke an internal server error
          Then The draft account response returns 500
