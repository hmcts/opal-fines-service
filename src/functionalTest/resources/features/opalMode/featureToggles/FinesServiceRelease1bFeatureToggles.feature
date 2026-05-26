@Opal @UAT-Technical @JIRA-LABEL:account-enquiry @JIRA-LABEL:reference-data
Feature: Fines Service Release 1b Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1BOff @JIRA-STORY:PO-3762 @JIRA-EPIC:PO-3685
  Scenario Outline: Release 1b gated endpoint is unavailable when release 1b is disabled
    When I call the release 1b gated endpoint "<endpoint>"
    Then the request is rejected with status 405
    And the response reports that the feature is disabled

    Examples:
      | endpoint                                   |
      | Search Defendant Accounts                  |
      | Search Minor Creditor Accounts             |
      | Get Defendant Account Header Summary       |
      | Get Defendant Account At A Glance          |
      | Update Defendant Account                   |
      | Add Note                                   |
      | Get Defendant Account Party                |
      | Replace Defendant Account Party            |
      | Add Defendant Account Party                |
      | Remove Defendant Account Party             |
      | Get Defendant Account Enforcement Status   |
      | Get Defendant Account Impositions          |
      | Add Defendant Account Enforcement          |
      | Remove Defendant Account Enforcement Hold  |
      | Get Defendant Account Payment Terms        |
      | Get Result                                 |
      | Add Defendant Account Payment Terms        |
      | Add Defendant Account Payment Card Request |
      | Get Defendant Account Fixed Penalty        |
      | Get Minor Creditor Account Header Summary  |
      | Get Minor Creditor Account At A Glance     |
      | Get Minor Creditor Account                 |

  @R1B @JIRA-STORY:PO-3762 @JIRA-EPIC:PO-3685
  Scenario: Get Defendant Account Impositions is available when release 1b is enabled
    When I call the release 1b gated endpoint "Get Defendant Account Impositions"
    Then the request is rejected as not found
