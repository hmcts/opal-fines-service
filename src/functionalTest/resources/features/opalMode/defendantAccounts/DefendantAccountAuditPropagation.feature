@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Audit Propagation

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @JIRA-STORY:PO-2623 @JIRA-EPIC:PO-2621
  Scenario: E2E.01 PATCH defendant account populates audit with user id and username
    Given an auditable defendant account exists for submitted by "DEFAUD001"
    And the created defendant account has no amendment history yet
    When I patch the created defendant account with an enforcement override
    Then the defendant account patch response matches the documented contract
    And the created defendant account amendment history is recorded for business user "L077JG" and username "opal-test@dev.platform.hmcts.net"
    And the created defendant account amendment history contains new value "FWEC"

  @cleanUpData @JIRA-STORY:PO-2623 @JIRA-EPIC:PO-2621
  Scenario: E2E.02 POST payment terms requesting a payment card propagates authentication into audit
    Given an auditable defendant account exists for submitted by "DEFAUD002"
    And the created defendant account has no amendment history yet
    When I add payment terms requesting a payment card for the created defendant account
    Then the payment terms response matches the documented contract

  @cleanUpData @JIRA-STORY:PO-2623 @JIRA-EPIC:PO-2621
  Scenario: E2E.03 PUT replace defendant account party populates audit correctly
    Given an auditable defendant account exists for submitted by "DEFAUD003"
    And the created defendant account has no amendment history yet
    When I replace the defendant party for the created defendant account
    Then the replace defendant account party response matches the documented contract
    And the created defendant account amendment history is recorded for business user "L077JG" and username "opal-test@dev.platform.hmcts.net"
    And the created defendant account amendment history contains new value "Audit Party Replace"
