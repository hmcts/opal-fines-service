@PO-184 @PO-187 @PO-188 @PO-192 @Opal
Feature: tests for changes to postedBy and postedByAAD

  Scenario: postedBy and postedByAAD are correct for the different users
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | businessUnitId     | 73                                       |
      | noteText           | test postedBy and PostedByAAD Opal user1 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user1 |
      | postedBy           | L073JG                                   |
      | postedByUserId     | 500000000                                |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | businessUnitId     | 71                                       |
      | noteText           | test postedBy and PostedByAAD Opal user2 |
    Then the add notes request is forbidden

    Given I am testing as the "opal-test-3@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | businessUnitId     | 26                                       |
      | noteText           | test postedBy and PostedByAAD Opal user3 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user3 |
      | postedBy           | L026SH                                   |
      | postedByUserId     | 500000002                                |

    Given I am testing as the "opal-test-4@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | businessUnitId     | 47                                       |
      | noteText           | test postedBy and PostedByAAD Opal user4 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user4 |
      | postedBy           | L047SA                                   |
      | postedByUserId     | 500000003                                |
