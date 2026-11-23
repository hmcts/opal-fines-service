@Legacy @R1BDrop2 @JIRA-LABEL:account-enquiry @JIRA-DEFECT:PO-11471 @MinorCreditorSearch @JIRA-STORY:PO-11471 @JIRA-EPIC:PO-11478
Feature: Minor Creditor Account Search API in Legacy Mode

  Scenario: Minor creditor search returns the creditor details from a published account
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a published defendant account exists with an individual minor creditor for search
    When I search for the individual minor creditor created for this scenario
    Then the minor creditor search response contains the created Legacy creditor

  Scenario: Minor creditor search returns multiple matching accounts
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And two published defendant accounts exist with individual minor creditors for search
    When I search for the individual minor creditor created for this scenario
    Then the minor creditor search response contains both created Legacy creditors
