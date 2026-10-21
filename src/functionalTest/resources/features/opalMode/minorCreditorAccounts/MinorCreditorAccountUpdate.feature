@Opal @R1BDrop2 @JIRA-LABEL:account-enquiry @MinorCreditorUpdatePdpo
Feature: PDPO logging for minor creditor account updates

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-10765 @JIRA-EPIC:PO-10729
  Scenario: Updating an individual minor creditor emits an alteration log
    Given an individual minor creditor account exists for PDPO update logging
    When I update the created minor creditor account details
    Then the minor creditor update returns the changed details and preserves version behaviour
    And the logging service emits "Update Minor Creditor" with category "Alteration" for the created minor creditor account

  @JIRA-STORY:PO-10765 @JIRA-EPIC:PO-10729
  Scenario: Updating a company minor creditor does not emit an individual alteration log
    Given a company minor creditor account exists for PDPO update logging
    When I update the created minor creditor account details
    Then the minor creditor update returns the changed details and preserves version behaviour
    And the logging service does not emit "Update Minor Creditor" for the created minor creditor account
