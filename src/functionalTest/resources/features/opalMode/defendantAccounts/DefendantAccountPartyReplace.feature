@Opal @JIRA-LABEL:account-enquiry @R1BDrop1
Feature: PDPO logging for defendant account party replacement

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @JIRA-STORY:PO-10767 @JIRA-EPIC:PO-10729
  Scenario: Updating an individual defendant emits an alteration log
    Given an auditable defendant account exists for submitted by "PDPOUPD001"
    When I replace the defendant party for the created defendant account
    Then the replace defendant account party response matches the documented contract
    And the logging service emits "Update Defendant" with category "Alteration" for the "Defendant" party

  @cleanUpData @JIRA-STORY:PO-10767 @JIRA-EPIC:PO-10729
  Scenario: Converting an individual to a company emits conversion and guardian removal logs
    Given an auditable defendant account exists for submitted by "PDPOCON001"
    When I add a non-paying Parent/Guardian to the created defendant account
    Then the add-party response identifies a non-paying Parent/Guardian with a party ID
    When I convert the defendant party for the created defendant account to a company
    Then the converted defendant party response identifies a company
    And the logging service emits "Convert Individual to Company" with category "Erasure" for the "Defendant" party
    And the logging service emits "Remove Parent/Guardian" with category "Erasure" for the "Parent/Guardian" party
