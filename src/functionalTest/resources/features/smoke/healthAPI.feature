@Smoke
Feature: Health API

  Scenario: The health endpoint reports that the service is up
    When I request the fines api health status
    Then the fines service reports as up
