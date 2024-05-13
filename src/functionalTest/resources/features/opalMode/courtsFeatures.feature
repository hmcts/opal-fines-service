Feature: Test the Court data for end points

  @PO-315
  #PO-315
  Scenario: Checking the end points for court ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with
    Then the court ref data response is 200
