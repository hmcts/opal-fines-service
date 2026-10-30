@Opal @JIRA-LABEL:manual-account-creation @R1A @JIRA-DEFECT:PO-11118 @JIRA-STORY:11192 @JIRA-EPIC:PO-50 @ignore
Feature: Create accounts with maximum-length fields

  @cleanUpData
  Scenario: Published individual account retains maximum-length header values
    Given a defendant account with header summary data exists for submitted by "MAXLEN-IND-001" using fixture "draftAccounts/accountJson/individualMaxLengths.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains originator type "TFO"
    And the defendant account header summary contains originator name "Humber Magistrates' Court"
    And the defendant account header summary contains collection order "true"
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains party details forenames "FFFFFFFFFFFFFFFFFFFF" surname "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | PCR-11111111111111111111111111 |
