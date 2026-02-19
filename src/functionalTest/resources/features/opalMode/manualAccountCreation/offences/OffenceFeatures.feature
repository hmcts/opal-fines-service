Feature: Test the Offence Reference data for end points

  @PO-311 @Opal
  #PO-311
  @JIRA-KEY:POT-243
  Scenario: Checking the end points for Offence ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering by cjs code "AA06"
    Then the offence ref data matching to result
