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
      | defendant_account_id | 500000009              |
      | account_number       | 80000000000I           |
      | full_name            | Mr Smart D John        |
      | address              | 10 Brooks Lake, Cobham |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account details api with
      | defendantID | 500000009 |

    Then the response from the defendant account details api is forbidden

  Scenario: Get Notes endpoint
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to get the defendant account notes for
      | defendantID | 500000009 |
    Then the response contains the following in position "0"
      | associated_record_id | 500000009                   |
      | note_text            | Comment for Notes 500000009 |
    Then the response contains the following in position "1"
      | associated_record_id | 500000009                   |
      | note_text            | Comment for Notes 500000010 |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to get the defendant account notes for
      | defendantID | 500000009 |
    Then the get notes request is forbidden

  Scenario: Add Notes endpoint
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associated_record_id    | 500000010                       |
      | business_unit_id        | 73                              |
      | note_text               | test roles and perms Opal user1 |
    And the add notes response contains
      | associated_record_id   | 500000010                       |
      | note_text              | test roles and perms Opal user1 |
      | posted_by              | L073JG                          |
      | posted_by_user_id      | 500000000                       |
      | business_unit_id       | 73                              |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associated_record_id | 500000010                       |
      | business_unit_id     | 71                              |
      | note_text            | test roles and perms Opal user1 |
    Then the add notes request is forbidden
