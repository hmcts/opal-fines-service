@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account Search Feature Toggles

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @R1BOff @JIRA-STORY:PO-3768 @JIRA-STORY:PO-3762 @JIRA-EPIC:PO-3685 @JIRA-TEST-KEY:PO-8368
  Scenario: Search endpoint is unavailable when release 1b is disabled
    When I search defendant accounts using prosecutor case reference "R1B-OFF-CHECK" without consolidation
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

  @R1COff @JIRA-STORY:PO-3768 @JIRA-EPIC:PO-3685
  Scenario: Basic search remains available when release 1c is disabled
    Given a searchable defendant account exists for feature-toggle search
    When I search the created defendant account without consolidation
    Then the basic defendant account search returns the created account

  @R1COff @JIRA-STORY:PO-3768 @JIRA-EPIC:PO-3685
  Scenario: Consolidated search is unavailable when release 1c is disabled
    Given a searchable defendant account exists for feature-toggle search
    When I search the created defendant account with consolidation
    Then the request is rejected with status 404
    And the response reports that the feature is disabled

  @R1B @JIRA-STORY:PO-3768 @JIRA-EPIC:PO-3685
  Scenario: Consolidated search is available when release 1c is enabled
    Given a searchable defendant account exists for feature-toggle search
    When I search the created defendant account with consolidation
    Then the consolidated defendant account search returns the created account
