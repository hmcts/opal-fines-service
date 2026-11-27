@Opal @JIRA-LABEL:account-enquiry @cleanUpData @JIRA-STORY:PO-11194 @JIRA-EPIC:PO-2621
Feature: Activity Notes

  @R1BDrop1
  Scenario: Add a maximum-length activity note to a defendant account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And an enforceable defendant account exists with the following details
      | source            | MAC                                         |
      | business_unit_id  | 77                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | DEFNOTE11194                                |
      | submitted_by_name | Laura Clerk                                 |
    And a valid activity-note fixture containing exactly 1000 characters
    When I submit the activity-note fixture for the created defendant account
    And I request defendant account history for the created defendant account
    Then the account history contains the submitted activity note with exactly 1000 characters

  @R1BDrop2 @MinorCreditorHistory
  Scenario: Add a maximum-length activity note to a minor-creditor account
    Given a minor creditor account with representative history exists for submitted by "MCNOTE11194"
    And a valid activity-note fixture containing exactly 1000 characters
    When I submit the activity-note fixture for the created minor-creditor account
    And I request minor creditor account history for the created minor creditor account
    Then the account history contains the submitted activity note with exactly 1000 characters
