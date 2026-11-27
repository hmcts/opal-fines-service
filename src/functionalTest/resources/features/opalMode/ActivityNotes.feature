@Opal @JIRA-LABEL:account-enquiry @cleanUpData @JIRA-STORY:PO-11194 @JIRA-EPIC:PO-2621
Feature: Activity Notes

  @R1BDrop1
  Scenario: Add a maximum-length activity note to a defendant account
    Given a defendant account with history exists for submitted by "DEFNOTE11194"
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
