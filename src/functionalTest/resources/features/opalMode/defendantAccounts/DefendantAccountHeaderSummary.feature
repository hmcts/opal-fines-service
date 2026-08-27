@Opal @JIRA-LABEL:account-enquiry @R1B
Feature: Defendant Account Header Summary

  @cleanUpData @JIRA-STORY:PO-2964 @JIRA-EPIC:PO-2630
  Scenario: E2E.01 Header summary returns stored values for a live adult defendant account
    Given a defendant account with header summary data exists for submitted by "PO2964-TFO-001" using fixture "draftAccounts/accountJson/adultAccount.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains originator type "TFO"
    And the defendant account header summary contains originator name "Humber Magistrates' Court"
    And the defendant account header summary contains collection order "true"
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains party details forenames "FNAME" surname "LNAME"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | 12345 |

  @cleanUpData @JIRA-STORY:PO-2964 @JIRA-EPIC:PO-2630
  Scenario: E2E.02 Header summary returns stored values for a live parent or guardian defendant account
    Given a defendant account with header summary data exists for submitted by "PO2964-TFO-002" using fixture "draftAccounts/accountJson/parentOrGuardianAccount.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains originator type "TFO"
    And the defendant account header summary contains originator name "Humber Magistrates' Court"
    And the defendant account header summary contains collection order "false"
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains party details forenames "FNAME" surname "LNAME"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | 33333 |

  @cleanUpData @JIRA-STORY:PO-2964 @JIRA-EPIC:PO-2630
  Scenario: E2E.03 Header summary requests without a token are rejected
    Given a defendant account with header summary data exists for submitted by "PO2964-AUTH-001" using fixture "draftAccounts/accountJson/adultAccount.json"
    When I request defendant account header summary for the created defendant account without a token
    Then the response status code is 401
    And the defendant account header summary error response matches the standard problem detail contract for status 401
    And the defendant account header summary error title contains "Unauthorized"
    And the defendant account header summary error is non-retriable

  @cleanUpData @JIRA-STORY:PO-2964 @JIRA-EPIC:PO-2630
  Scenario: E2E.04 Header summary requests from an unauthorized user are rejected
    Given a defendant account with header summary data exists for submitted by "PO2964-AUTH-002" using fixture "draftAccounts/accountJson/adultAccount.json"
    When the "opal-test-2@dev.platform.hmcts.net" user requests defendant account header summary for the created defendant account
    Then the response status code is 403
    And the defendant account header summary error response matches the standard problem detail contract for status 403
    And the defendant account header summary error title contains "Forbidden"
    And the defendant account header summary error is non-retriable

  @cleanUpData @JIRA-STORY:PO-2964 @JIRA-EPIC:PO-2630
  Scenario: E2E.05 Non-existent defendant account returns not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request defendant account header summary for a non-existent defendant account
    Then the response status code is 404
    And the defendant account header summary error response matches the standard problem detail contract for status 404
    And the defendant account header summary error title contains "Entity Not Found"
    And the defendant account header summary error is non-retriable
    And the defendant account header summary error response does not leak internal details
