Feature: Test the Court data for end points

  @PO-315 @Opal
  #PO-315
  Scenario: Checking the end points for court ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with a filter of "Yeovil FPC"
    Then the response contains the correct court data when filtered by court name "Yeovil FPC"
