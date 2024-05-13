Feature: Test the LJA Reference data for end points

 #PO-312
 Scenario: Checking the end points for LJA ref data
 Given I am testing as the "opal-test@hmcts.net" user
 When I make a request to the LJA ref data api with
 Then the LJA ref data response is 200
