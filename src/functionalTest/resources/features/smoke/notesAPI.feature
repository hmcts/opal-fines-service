@Smoke
Feature: Notes API tests
  Scenario: I post some data to the Notes API and the response contains the correct Data - null noteID
    Given I post the following data to the notes API
    |recordId   |123123                   |
    |recordType |def_account              |
    |noteText   |Auto Test Note           |
    |noteType   |NT                       |
    |postedBy   |Auto Test                |
    |postedDate |2023-12-07T11:21:48.677Z |
    |noteID     |                         |

