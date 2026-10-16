@Opal @JIRA-LABEL:account-enquiry @R1BDrop1
Feature: Add a defendant account party

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @JIRA-STORY:PO-10745 @JIRA-EPIC:PO-10729
  Scenario: Add a non-paying Parent/Guardian to a defendant account
    Given an auditable defendant account exists for submitted by "PDPOPG001"
    When I add a non-paying Parent/Guardian to the created defendant account
    Then the add-party response identifies a non-paying Parent/Guardian with a party ID
