@PO-118 @PO-120 @Legacy @Ignore
Feature: Test the defendant account search API Legacy

  Scenario: data is returned from the stub when in legacy mode
    When I make a call to the defendant search API using the parameters
      | forename       |  |
      | surname        |  |
      | initials       |  |
      | dayOfMonth     |  |
      | monthOfYear    |  |
      | year           |  |
      | addressLineOne |  |
    Then there is one result returned matching
      | name         | Sir Albert MBE Jones |
      | dateOfBirth  | 1988-08-28           |
      | addressLine1 | Wales                |






