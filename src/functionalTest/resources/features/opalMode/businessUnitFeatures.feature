
@Opal @PO-313
Feature: Verifying the end points for business units


  Scenario: verifying the end points for API for Business Units Ref Data
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the business unit ref data api
    Then the business unit ref data matching to result

