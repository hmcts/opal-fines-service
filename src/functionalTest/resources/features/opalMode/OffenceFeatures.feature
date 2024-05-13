Feature: Test the Offence Reference data for end points

  #PO-311
  Scenario: Checking the end points for Offence ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api with
    Then the offence ref data response is 200
