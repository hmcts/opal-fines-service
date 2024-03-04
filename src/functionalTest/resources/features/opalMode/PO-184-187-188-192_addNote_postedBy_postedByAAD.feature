@PO-184 @PO-187 @PO-188 @PO-192 @Opal
Feature: tests for changes to postedBy and postedByAAD

  Scenario: postedBy and postedByAAD are correct for the different users
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user1 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user1 |
      | postedBy           | L070KG                                   |
      | postedByAAD        | gl.userfour                              |

    Given I am testing as the "opal-test-2@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user2 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user2 |
      | postedBy           | opal-test-2@HMCTS.NE                     |
      | postedByAAD        | gl.firstuser                             |

    Given I am testing as the "opal-test-3@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user3 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user3 |
      | postedBy           | L024US                                   |
      | postedByAAD        | Suffolk.user                             |

    Given I am testing as the "opal-test-4@hmcts.net" user
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user4 |
    And the add notes response contains
      | associatedRecordId | 500000010                                |
      | noteText           | test postedBy and PostedByAAD Opal user4 |
      | postedBy           | L096GH                                   |
      | postedByAAD        | humber.usertwo                           |
