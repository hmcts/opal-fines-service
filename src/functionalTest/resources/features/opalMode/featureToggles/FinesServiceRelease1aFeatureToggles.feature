@Opal @FeatureToggle @JIRA-LABEL:reference-data @JIRA-LABEL:manual-account-creation
Feature: Fines Service Release 1a Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1AOff @JIRA-STORY:PO-3754 @JIRA-EPIC:PO-3685
  Scenario Outline: Release 1a gated endpoint is unavailable when release 1a is disabled
    When I call the release 1a gated endpoint "<endpoint>"
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

    @JIRA-TEST-KEY:PO-8840
    Examples:
      | endpoint              |
      | Get Courts            |
    @JIRA-TEST-KEY:PO-8841
    Examples:
      | endpoint              |
      | Search Courts         |
    @JIRA-TEST-KEY:PO-8842
    Examples:
      | endpoint              |
      | Get Business Units    |
    @JIRA-TEST-KEY:PO-8843
    Examples:
      | endpoint              |
      | Get Business Unit     |
    @JIRA-TEST-KEY:PO-8844
    Examples:
      | endpoint              |
      | Get Local Justice Areas |
    @JIRA-TEST-KEY:PO-8845
    Examples:
      | endpoint              |
      | Get Offences          |
    @JIRA-TEST-KEY:PO-8846
    Examples:
      | endpoint              |
      | Get Offence           |
    @JIRA-TEST-KEY:PO-8847
    Examples:
      | endpoint              |
      | Search Offences       |
    @JIRA-TEST-KEY:PO-8848
    Examples:
      | endpoint              |
      | Get Major Creditors   |
    @JIRA-TEST-KEY:PO-8849
    Examples:
      | endpoint              |
      | Get Prosecutors       |
    @JIRA-TEST-KEY:PO-8850
    Examples:
      | endpoint              |
      | Add Draft Account     |
    @JIRA-TEST-KEY:PO-8851
    Examples:
      | endpoint              |
      | Get Draft Accounts    |
    @JIRA-TEST-KEY:PO-8852
    Examples:
      | endpoint              |
      | Get Draft Account     |
    @JIRA-TEST-KEY:PO-8853
    Examples:
      | endpoint              |
      | Replace Draft Account |
    @JIRA-TEST-KEY:PO-8854
    Examples:
      | endpoint              |
      | Update Draft Account  |
