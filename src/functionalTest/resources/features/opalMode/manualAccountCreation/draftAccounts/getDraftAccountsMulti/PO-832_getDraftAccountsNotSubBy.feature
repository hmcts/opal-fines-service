#@Opal
#Feature: PO-832 get draft accounts not submitted by
#
#  @PO-832 @cleanUpData
#  Scenario: Get draft accounts not submitted by - happy path
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I create a draft account with the following details
#      | business_unit_id  | 73                                          |
#      | account           | draftAccounts/accountJson/adultAccount.json |
#      | account_type      | Fine                                        |
#      | account_status    | Submitted                                   |
#      | submitted_by      | BUUID                                       |
#      | submitted_by_name | Laura Clerk                                 |
#      | timeline_data     | draftAccounts/timelineJson/default.json     |
#      | version           | 0                                           |
#    Then The draft account response returns 201
#    And I store the created draft account ID
#
#    When I create a draft account with the following details
#      | business_unit_id  | 73                                          |
#      | account           | draftAccounts/accountJson/adultAccount.json |
#      | account_type      | Fine                                        |
#      | account_status    | Submitted                                   |
#      | submitted_by      | BUUID_TWO                                   |
#      | submitted_by_name | Laura Clerk                                 |
#      | timeline_data     | draftAccounts/timelineJson/default.json     |
#      | version           | 0                                           |
#    Then The draft account response returns 201
#    And I store the created draft account ID
#
#    When I get the draft accounts filtering on Not Submitted by "BUUID" then the response contains
#      | account_snapshot.submitted_by | BUUID_TWO |
#    And The draft account filtered response does not contain accounts submitted by "BUUID"

#    @PO-832 @cleanUpData
#    Scenario: Get draft accounts both not submitted by and submitted by parameters
#      Given I am testing as the "opal-test@hmcts.net" user
#      When I create a draft account with the following details
#        | business_unit_id  | 73                                          |
#        | account           | draftAccounts/accountJson/adultAccount.json |
#        | account_type      | Fine                                        |
#        | account_status    | Submitted                                   |
#        | submitted_by      | BUUID                                       |
#        | submitted_by_name | Laura Clerk                                 |
#        | timeline_data     | draftAccounts/timelineJson/default.json     |
#        | version           | 0                                           |
#      Then The draft account response returns 201
#      And I store the created draft account ID
#
#      When I create a draft account with the following details
#        | business_unit_id  | 73                                          |
#        | account           | draftAccounts/accountJson/adultAccount.json |
#        | account_type      | Fine                                        |
#        | account_status    | Submitted                                   |
#        | submitted_by      | BUUID_TWO                                   |
#        | submitted_by_name | Laura Clerk                                 |
#        | timeline_data     | draftAccounts/timelineJson/default.json     |
#        | version           | 0                                           |
#      Then The draft account response returns 201
#      And I store the created draft account ID
#
#      When I get the draft accounts filtering on Submitted by "BUUID" and Not Submitted by "BUUID_TWO"
#      Then The draft account response returns 400
