@PO-250 @Opal
Feature: Authorisation on endpoints

  Scenario: Defendant Account Search endpoint
    Given I am testing as the "opal-test@hmcts.net" user
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

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a call to the defendant search API using the parameters
      | forename    | Smart          |
      | surname     | John           |
      | initials    | D              |
      | dayOfMonth  | 23             |
      | monthOfYear | 11             |
      | year        | 1999           |
      | addressLine | 10 Brooks Lake |
    Then the response from the defendant account search api is forbidden

  Scenario: Defendant Account Id endpoint
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account details api with
      | defendantID | 500000009 |

    Then the response from the defendant account details api is
      | defendantAccountId | 500000009              |
      | accountNumber      | 80000000000I           |
      | fullName           | Mr Smart D John        |
      | address            | 10 Brooks Lake, Cobham |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account details api with
      | defendantID | 500000009 |

    Then the response from the defendant account details api is forbidden

  Scenario: Get Notes endpoint
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to get the defendant account notes for
      | defendantID | 500000009 |
    Then the response contains the following in position "0"
      | associatedRecordId | 500000009                   |
      | noteText           | Comment for Notes 500000009 |
    Then the response contains the following in position "1"
      | associatedRecordId | 500000009                   |
      | noteText           | Comment for Notes 500000010 |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to get the defendant account notes for
      | defendantID | 500000009 |
    Then the get notes request is forbidden

  Scenario: Add Notes endpoint
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                       |
      | businessUnitId     | 17                              |
      | noteText           | test roles and perms Opal user1 |
    And the add notes response contains
      | associatedRecordId | 500000010                       |
      | noteText           | test roles and perms Opal user1 |
      | postedBy           | L017KG                          |
      | postedByUserId     | 500000000                       |
      | businessUnitId     | 17                              |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                       |
      | businessUnitId     | 17                              |
      | noteText           | test roles and perms Opal user1 |
    Then the add notes request is forbidden
