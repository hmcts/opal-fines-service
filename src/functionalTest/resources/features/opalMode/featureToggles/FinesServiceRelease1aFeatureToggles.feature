@Opal @UAT-Technical @JIRA-LABEL:reference-data @JIRA-LABEL:manual-account-creation
Feature: Fines Service Release 1a Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1AOff @JIRA-STORY:PO-3754 @JIRA-EPIC:PO-3685
  Scenario Outline: Release 1a gated endpoint is unavailable when release 1a is disabled
    When I call the release 1a gated endpoint "<endpoint>"
    Then the request is rejected with status 405
    And the response reports that the feature is disabled

    Examples:
      | endpoint              |
      | Get Courts            |
      | Search Courts         |
      | Get Business Units    |
      | Get Business Unit     |
      | Get Local Justice Areas |
      | Get Offences          |
      | Get Offence           |
      | Search Offences       |
      | Get Major Creditors   |
      | Get Prosecutors       |
      | Add Draft Account     |
      | Get Draft Accounts    |
      | Get Draft Account     |
      | Replace Draft Account |
      | Update Draft Account  |
