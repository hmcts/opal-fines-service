@PO-235 @Opal
Feature: tests for notes business unit users/permissions for accounts dependant on business units

  Scenario: A user can add a note to a business unit it is part of
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associated_record_id | 500000010                       |
      | business_unit_id     | 73                              |
      | note_text            | test roles and perms Opal user1 |
    And the add notes response contains
      | associated_record_id | 500000010                       |
      | note_text            | test roles and perms Opal user1 |
      | posted_by            | L073JG                          |
      | posted_by_user_id    | 500000000                       |
      | business_unit_id     | 73                              |


    When I make a request to the defendant account add notes api with
      | associated_record_id | 500000010                       |
      | business_unit_id     | 77                              |
      | note_text            | test roles and perms Opal user1 |
    And the add notes response contains
      | associated_record_id | 500000010                       |
      | note_text            | test roles and perms Opal user1 |
      | posted_by            | L077JG                          |
      | posted_by_user_id    | 500000000                       |
      | business_unit_id     | 77                              |

  Scenario: A user cannot add a note to a business unit it is not part of
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associated_record_id | 500000010                       |
      | business_unit_id     | 24                              |
      | note_text            | test roles and perms Opal user1 |
    Then the add notes request is forbidden

  Scenario: The business unit must be defined in the request
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associated_record_id | 500000010                       |
      | note_text            | test roles and perms Opal user1 |
    #When error logging is looked at would be good to check the error message
    Then the add notes request is forbidden
