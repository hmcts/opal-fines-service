@ignore
@PO-130
Feature: Test the defendant account details API

  Scenario: Correct data returned when an existing account ID is used
    When I make a request to the defendant account details api with
      | defendantID | 500000009 |

    Then the response from the defendant account details api is
      | defendantAccountId | 500000009              |
      | accountNumber      | 80000000000I           |
      | fullName           | Mr Smart D John        |
      | address            | 10 Brooks Lake, Cobham |


  Scenario Outline: No data returned when a non-existent account ID is used
    When I make a request to the defendant account details api with
      | defendantID | <defendantID> |

    Then the response from the defendant account details api is empty
    Examples:
      | defendantID |
      |             |
      | 999999999   |
      | 123         |
      | 50000000L   |


