@Opal @JIRA-LABEL:interface-jobs @JIRA-EPIC:PO-304
Feature: Interface Jobs create API

  @JIRA-STORY:PO-2577 @JIRA-TEST-KEY:PO-2577-E2E-01
  Scenario: Reject POST without token
    When I call POST /interface-jobs without a token
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-2577 @JIRA-TEST-KEY:PO-2577-E2E-01B
  Scenario: Reject POST without permission
    When I call POST /interface-jobs as a user without permission
    Then the request is rejected as forbidden

  @JIRA-STORY:PO-2577 @JIRA-TEST-KEY:PO-2577-E2E-02
  Scenario: Create interface jobs
    When I submit the interface jobs create happy path request
    Then the interface jobs create response matches the documented schema

  @JIRA-STORY:PO-2577 @JIRA-TEST-KEY:PO-2577-E2E-03
  Scenario: Roll back failed create
    When I submit the interface jobs rollback request
    Then the interface jobs rollback request leaves no partial data behind
