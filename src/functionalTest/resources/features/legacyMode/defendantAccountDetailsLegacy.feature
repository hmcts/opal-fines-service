@PO-129 @JIRA-STORY:130 @PO-131 @Legacy @R1BDrop1 @JIRA-EPIC:PO-33 @Ignore
Feature: Defendant Account Details API In Legacy Mode

  Scenario: Existing defendant account details are returned from the legacy stub
    When I make a request to the defendant account details api with
      | defendantID | 12345 |

    Then the response from the defendant account details api is
      | defendantAccountId | 12345                   |
      | accountNumber      | ACCT-123                |
      | fullName           | Mr. John Doe            |
      | address            | 123 Main Street, Apt 4B |
