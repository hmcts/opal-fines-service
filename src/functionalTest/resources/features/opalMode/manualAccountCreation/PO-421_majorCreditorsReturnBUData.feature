  Feature: Major Creditors Api returns Business unit level Data
    @Opal @PO-421
    Scenario: Major Creditors Api returns Business unit level Data
      Given I am testing as the "opal-test@hmcts.net" user
      When I make a request to the major creditors ref data api filter by major creditor id 15
      Then the response contains the correct major creditor data when filtered by id 15

    @Opal @PO-421
    Scenario: Major Creditors Api returns Business unit level Data - negative test
      Given I am testing as the "opal-test@hmcts.net" user
      When I make a request to the major creditors ref data api filter by major creditor id 16
      Then the response does not contain the major creditor data for 15
