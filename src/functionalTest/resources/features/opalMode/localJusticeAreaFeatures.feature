Feature: Test the LJA Reference data for end points

  @PO-312 @Opal
 #PO-312
  @JIRA-KEY:POT-158
  Scenario: Checking the end points for LJA ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result
