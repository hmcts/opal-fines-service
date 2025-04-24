@PO-183 @Opal
Feature: tests for authorisation on the defendant account search endpoint

#  Scenario: authorised user can make the request
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a call to the defendant search API using the parameters
#      | forename    | Smart          |
#      | surname     | John           |
#      | initials    | D              |
#      | dayOfMonth  | 23             |
#      | monthOfYear | 11             |
#      | year        | 1999           |
#      | addressLine | 10 Brooks Lake |
#    Then there is one result returned matching
#      | name         | Mr Smart D John |
#      | dateOfBirth  | 1999-11-23      |
#      | addressLine1 | 10 Brooks Lake  |

  Scenario: unauthorised user cannot make the request
    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a call to the defendant search API using the parameters
      | forename    | Smart          |
      | surname     | John           |
      | initials    | D              |
      | dayOfMonth  | 23             |
      | monthOfYear | 11             |
      | year        | 1999           |
      | addressLine | 10 Brooks Lake |
    Then the add notes request is forbidden
