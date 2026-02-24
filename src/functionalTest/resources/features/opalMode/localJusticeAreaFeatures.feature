Feature: Test the LJA Reference data for end points

  @PO-312 @Opal
 #PO-312
  Scenario: Checking the end points for LJA ref data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @PO-2757
  Scenario: Happy path without lja_type returns all LJAs
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the LJA ref data api with
    Then the response status code is 200

  @PO-2757
  Scenario: Deployed filtering with single lja_type
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the LJA ref data api with lja_type "LJA"
    Then the response status code is 200
    And all returned LJAs have lja_type "LJA"

  @PO-2757
  Scenario: Deployed filtering with multiple lja_type values
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the LJA ref data api with lja_type "LJA,CRWCRT"
    Then the response status code is 200
    And all returned LJAs have lja_type in "LJA,CRWCRT"





