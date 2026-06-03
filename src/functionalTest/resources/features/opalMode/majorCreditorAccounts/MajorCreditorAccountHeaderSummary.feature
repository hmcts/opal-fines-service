@Opal @JIRA-LABEL:account-enquiry
Feature: Major Creditor Account Header Summary

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary is returned for a valid request
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request the major creditor account header summary for account 10770000000041
    Then the major creditor account header summary request succeeds
    And the major creditor account header summary matches the documented contract
    And the major creditor account header summary response contains only documented fields
    And the major creditor account header summary ETag is "1"

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary returns the same body and ETag for repeated requests
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request the major creditor account header summary for account 10770000000041 twice
    Then the repeated major creditor account header summary responses are identical

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary is forbidden when the user has no account view permission
    When the "opal-test-2@dev.platform.hmcts.net" user requests the major creditor account header summary for account 10770000000041
    Then the request is rejected as forbidden

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary requests without a token are rejected
    When I request the major creditor account header summary for account 10770000000041 without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary requests with an invalid token are rejected
    When I request the major creditor account header summary for account 10770000000041 with an invalid token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2136 @JIRA-EPIC:FAE-View-Major-Creditor-Account-Summary
  Scenario: Major creditor account header summary returns not found for an unknown account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request the major creditor account header summary for account 999999
    Then the request is rejected as not found
