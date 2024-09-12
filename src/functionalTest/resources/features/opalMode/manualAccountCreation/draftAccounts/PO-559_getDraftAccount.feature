@Opal
Feature: PO-559 get draft account

  @PO-559 @cleanUpData
  Scenario: Get draft account - happy path
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     |                                             |
      | timeline_data    |                                             |
    Then The draft account response returns 201
    And I store the created draft account ID

    Then I get the single created draft account and the response contains
      | business_unit_id                  | 73                  |
      | account_type                      | Fine                |
      | account_status                    | Submitted           |
      | account_snapshot.DefendantName    | LNAME, FNAME        |
      | account_snapshot.DateOfBirth      | 01/01/2000          |
      | account_snapshot.AccountType      | Fine                |
      | account_snapshot.SubmittedBy      | opal-test@HMCTS.NET |
      | account_snapshot.BusinessUnitName | MBEC London         |

    Then I delete the created draft accounts


