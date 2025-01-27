#@PO-130 @Opal
#Feature: Test the defendant account details API Opal
#
#  Scenario: Correct data returned when an existing account ID is used
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a request to the defendant account details api with
#      | defendantID | 500000009 |
#
#    Then the response from the defendant account details api is
#      | defendant_account_id | 500000009              |
#      | account_number       | 80000000000I           |
#      | full_name            | Mr Smart D John        |
#      | address              | 10 Brooks Lake, Cobham |
#
#
#  Scenario Outline: No data returned when a non-existent account ID is used
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a request to the defendant account details api with
#      | defendantID | <defendantID> |
#
#    Then the response from the defendant account details api is empty
#    Examples:
#      | defendantID |
#      |             |
#      | 999999999   |
#      | 123         |
#      | 50000000L   |
