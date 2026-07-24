@Opal @FeatureToggle @Ignore @release-1c-write-off @JIRA-LABEL:account-enquiry @JIRA-LABEL:release-1c-write-off @JIRA-EPIC:PO-3685
Feature: Fines Service Release 1c Write-off Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1CWriteOffOff @Release1cOff @JIRA-STORY:PO-3768
  Scenario: Consolidated defendant account search is unavailable when release 1c write-off is disabled
    When I call the defendant account search endpoint with consolidated search enabled
    Then the request is rejected with status 405
    And the release 1c write-off feature-disabled response is returned

  @R1CWriteOffOff @Release1cOff @JIRA-STORY:PO-3768
  Scenario: Defendant account search remains available without consolidated search when release 1c write-off is disabled
    When I call the defendant account search endpoint without consolidated search
    Then the response status is 200

  @R1CWriteOff @Release1cOn @JIRA-STORY:PO-3768
  Scenario: Consolidated defendant account search is available when release 1c write-off is enabled
    When I call the defendant account search endpoint with consolidated search enabled
    Then the response status is 200
