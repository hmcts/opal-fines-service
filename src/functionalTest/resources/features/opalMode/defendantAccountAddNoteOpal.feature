@PO-139 @PO-138 @Opal
Feature: Test the add note endpoint for Opal

  Scenario: assert add note response is valid for opal route
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000000              |
      | noteText           | test account note Opal |
    Then the add notes response contains
      | associatedRecordId | 500000000              |
      | noteText           | test account note Opal |

  Scenario: latest added note response is returned in ac details request
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000000               |
      | noteText           | test account note2 Opal |
    And the add notes response contains
      | associatedRecordId | 500000000               |
      | noteText           | test account note2 Opal |
    When I make a request to the defendant account details api with
      | defendantID | 500000000 |
    Then the following account note is returned in the ac details request
      | accountNotes | test account note2 Opal1 |
