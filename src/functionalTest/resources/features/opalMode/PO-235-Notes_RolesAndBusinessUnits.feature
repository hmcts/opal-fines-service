@PO-235 @Opal
Feature: tests for notes roles/permissions for accounts dependant on business units

  Scenario: A user can add a note to a business unit it is part of
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


    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                       |
      | businessUnitId     | 69                              |
      | noteText           | test roles and perms Opal user1 |
    And the add notes response contains
      | associatedRecordId | 500000010                       |
      | noteText           | test roles and perms Opal user1 |
      | postedBy           | L069KG                          |
      | postedByUserId     | 500000000                       |
      | businessUnitId     | 69                              |

  Scenario: A user cannot add a note to a business unit it is not part of
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                       |
      | businessUnitId     | 24                              |
      | noteText           | test roles and perms Opal user1 |
    Then the add notes request is forbidden

  Scenario: The business unit must be defined in the request
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                       |
      | noteText           | test roles and perms Opal user1 |
    #When error logging is looked at would be good to check the error message
    Then the add notes request is forbidden
