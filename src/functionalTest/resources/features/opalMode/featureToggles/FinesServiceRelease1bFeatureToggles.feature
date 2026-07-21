@Opal @JIRA-LABEL:account-enquiry @JIRA-LABEL:reference-data
Feature: Fines Service Release 1b Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1BOff @JIRA-STORY:PO-3762 @JIRA-EPIC:PO-3685
  Scenario Outline: Release 1b gated endpoint is unavailable when release 1b is disabled
    When I call the release 1b gated endpoint "<endpoint>"
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

    @JIRA-TEST-KEY:PO-8369
    Examples:
      | endpoint                                   |
      | Search Defendant Accounts                  |
    @JIRA-TEST-KEY:PO-8370
    Examples:
      | endpoint                                   |
      | Search Minor Creditor Accounts             |
    @JIRA-TEST-KEY:PO-8371
    Examples:
      | endpoint                                   |
      | Get Defendant Account Header Summary       |
    @JIRA-TEST-KEY:PO-8372
    Examples:
      | endpoint                                   |
      | Get Defendant Account At A Glance          |
    @JIRA-TEST-KEY:PO-8373
    Examples:
      | endpoint                                   |
      | Update Defendant Account                   |
    @JIRA-TEST-KEY:PO-8374
    Examples:
      | endpoint                                   |
      | Add Note                                   |
    @JIRA-TEST-KEY:PO-8375
    Examples:
      | endpoint                                   |
      | Get Defendant Account Party                |
    @JIRA-TEST-KEY:PO-8376
    Examples:
      | endpoint                                   |
      | Replace Defendant Account Party            |
    @JIRA-TEST-KEY:PO-8377
    Examples:
      | endpoint                                   |
      | Add Defendant Account Party                |
    @JIRA-TEST-KEY:PO-8378
    Examples:
      | endpoint                                   |
      | Remove Defendant Account Party             |
    @JIRA-TEST-KEY:PO-8379
    Examples:
      | endpoint                                   |
      | Get Defendant Account Enforcement Status   |
    @JIRA-TEST-KEY:PO-8380
    Examples:
      | endpoint                                   |
      | Get Defendant Account Impositions          |
    @JIRA-TEST-KEY:PO-8381
    Examples:
      | endpoint                                   |
      | Add Defendant Account Enforcement          |
    @JIRA-TEST-KEY:PO-8382
    Examples:
      | endpoint                                   |
      | Remove Defendant Account Enforcement Hold  |
    @JIRA-TEST-KEY:PO-8383
    Examples:
      | endpoint                                   |
      | Get Defendant Account Payment Terms        |
    @JIRA-TEST-KEY:PO-8384
    Examples:
      | endpoint                                   |
      | Get Result                                 |
    @JIRA-TEST-KEY:PO-8385
    Examples:
      | endpoint                                   |
      | Add Defendant Account Payment Terms        |
    @JIRA-TEST-KEY:PO-8386
    Examples:
      | endpoint                                   |
      | Add Defendant Account Payment Card Request |
    @JIRA-TEST-KEY:PO-8387
    Examples:
      | endpoint                                   |
      | Get Defendant Account Fixed Penalty        |
    @JIRA-TEST-KEY:PO-8388
    Examples:
      | endpoint                                   |
      | Get Minor Creditor Account Header Summary  |
    @JIRA-TEST-KEY:PO-8389
    Examples:
      | endpoint                                   |
      | Get Minor Creditor Account At A Glance     |
    @JIRA-TEST-KEY:PO-8390
    Examples:
      | endpoint                                   |
      | Get Minor Creditor Account                 |
    Examples:
      | endpoint                                   |
      | Get Mappings                               |

  @R1B @JIRA-STORY:PO-2077 @JIRA-EPIC:PO-979
  Scenario: Get Defendant Account Impositions is available when release 1b is enabled
    When I call the release 1b gated endpoint "Get Defendant Account Impositions"
    Then the request is rejected as not found
