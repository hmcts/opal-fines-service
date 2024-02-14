@PO-129 @PO-131 @Legacy @Ignore
Feature: Test the defendant account details API Legacy

  Scenario: Correct data returned when an existing account ID is used
    When I make a request to the defendant account details api with
      | defendantID | 12345 |

    Then the response from the defendant account details api is
      | defendantAccountId | 12345                   |
      | accountNumber      | ACCT-123                |
      | fullName           | Mr. John Doe            |
      | address            | 123 Main Street, Apt 4B |


