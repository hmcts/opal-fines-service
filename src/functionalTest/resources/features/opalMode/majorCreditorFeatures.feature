#Feature: Verifying the end points for the Major Creditors
#
#  @PO-349 @Opal
#  Scenario: Verifying the end points for the major creditors
#    Given I am testing as the "opal-test@hmcts.net" user
#    When I make a request to the major creditors ref data api filter by major creditor id 15
#    Then the major creditors ref data matching to result
