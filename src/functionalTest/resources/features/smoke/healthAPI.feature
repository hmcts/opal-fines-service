@Smoke @JIRA-STORY:PO-125 @JIRA-EPIC:PO-2233
Feature: Health API

  @JIRA-TEST-KEY:PO-7874
  Scenario: The health endpoint reports that the service is up
    When I request the fines api health status
    Then the fines service reports as up
