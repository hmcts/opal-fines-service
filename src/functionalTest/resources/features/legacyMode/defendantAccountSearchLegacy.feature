@PO-118 @PO-120 @Legacy @Ignore
Feature: Defendant Account Search API In Legacy Mode

  Scenario: Defendant account search returns stubbed data in legacy mode
    When I make a call to the defendant search API using the parameters
      | forename       |  |
      | surname        |  |
      | initials       |  |
      | dayOfMonth     |  |
      | monthOfYear    |  |
      | year           |  |
      | addressLine    |  |
    Then there is one result returned matching
      | name         | Sir Albert MBE Jones |
      | dateOfBirth  | 1988-08-28           |
      | addressLine1 | Wales                |
