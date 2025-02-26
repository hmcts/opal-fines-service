#@PO-119 @Opal
#Feature: Test the defendant account search API Opal
#
#  Scenario: exact search with correct parameters - exact result match
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a call to the defendant search API using the parameters
#      | forename      | Smart          |
#      | surname       | John           |
#      | initials      | D              |
#      | day_of_month  | 23             |
#      | month_of_year | 11             |
#      | year          | 1999           |
#      | address_line  | 10 Brooks Lake |
#    Then there is one result returned matching
#      | name          | Mr Smart D John |
#      | dateOfBirth   | 1999-11-23      |
#      | addressLine1  | 10 Brooks Lake  |
#
#  Scenario: exact search with incorrect parameters - no result match
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a call to the defendant search API using the parameters
#      | forename      | Smart          |
#      | surname       | John           |
#      | initials      | D              |
#      | day_of_month  | 22             |
#      | month_of_year | 11             |
#      | year          | 1999           |
#      | address_line  | 10 Brooks Lake |
#    Then there are no results returned
#
#
#  Scenario: partial search on surname - results contain partial surname
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a call to the defendant search API using the parameters
#      | forename       | Smart          |
#      | surname        | Joh            |
#      | initials       | D              |
#      | day_of_month   | 23             |
#      | month_of_year  | 11             |
#      | year           | 1999           |
#      | address_line   | 10 Brooks Lake |
#    Then the returned results match
#      | name           | Mr Smart D John |
#      | dateOfBirth    | 1999-11-23      |
#      | addressLine1   | 10 Brooks Lake  |
#
#
#  Scenario: broad search - all relevant results returned
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a call to the defendant search API using the parameters
#      | forename       | il |
#      | surname        |    |
#      | initials       |    |
#      | day_of_month   |    |
#      | month_of_year  |    |
#      | year           |    |
#      | address_line   |    |
#    Then the returned results match
#      | name           | il |
#      | dateOfBirth    |    |
#      | addressLine1   |    |
