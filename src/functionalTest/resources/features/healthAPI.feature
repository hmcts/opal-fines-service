@ignore //TODO: These should be enabled after PO-165
@Smoke
Feature: test the application health API
  Scenario: I query the status of the health API
    Then I check the health of the fines api
