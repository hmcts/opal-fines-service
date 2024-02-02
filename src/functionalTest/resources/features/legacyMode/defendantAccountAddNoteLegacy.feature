@PO-141 @Legacy
Feature: Test the add note endpoint for Legacy

  Scenario: assert add note canned response is valid for Legacy route
    When I make a request to the defendant account add notes api with
      | associatedRecordId | 500000000          |
      | noteText           | test account note1 |
    Then the add notes response contains
      | noteId             |12345678  |
      | associatedRecordId | 500000000 |
      | noteText           | test account note1 |
