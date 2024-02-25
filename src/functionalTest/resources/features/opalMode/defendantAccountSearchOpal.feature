@PO-119 @Opal
Feature: Test the defendant account search API Opal

  Scenario: exact search with correct parameters - exact result match
    When I make a call to the defendant search API using the parameters
      | forename    | Smart          |
      | surname     | John           |
      | initials    | D              |
      | dayOfMonth  | 23             |
      | monthOfYear | 11             |
      | year        | 1999           |
      | addressLine | 10 Brooks Lake |
    Then there is one result returned matching
      | name         | Mr Smart D John |
      | dateOfBirth  | 1999-11-23      |
      | addressLine1 | 10 Brooks Lake  |

  Scenario: exact search with incorrect parameters - no result match
    When I make a call to the defendant search API using the parameters
      | forename    | Smart          |
      | surname     | John           |
      | initials    | D              |
      | dayOfMonth  | 22             |
      | monthOfYear | 11             |
      | year        | 1999           |
      | addressLine | 10 Brooks Lake |
    Then there are no results returned


  Scenario: partial search on surname - results contain partial surname
    When I make a call to the defendant search API using the parameters
      | forename    | Smart          |
      | surname     | Joh            |
      | initials    | D              |
      | dayOfMonth  | 23             |
      | monthOfYear | 11             |
      | year        | 1999           |
      | addressLine | 10 Brooks Lake |
    Then the returned results match
      | name         | Mr Smart D John |
      | dateOfBirth  | 1999-11-23      |
      | addressLine1 | 10 Brooks Lake  |


  Scenario: broad search - all relevant results returned
    When I make a call to the defendant search API using the parameters
      | forename    | il |
      | surname     |    |
      | initials    |    |
      | dayOfMonth  |    |
      | monthOfYear |    |
      | year        |    |
      | addressLine |    |
    Then the returned results match
      | name         | il |
      | dateOfBirth  |    |
      | addressLine1 |    |
