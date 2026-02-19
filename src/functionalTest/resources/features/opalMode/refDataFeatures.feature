@Opal
Feature: This feature covers all the ref data scenarios


  Background:
    Given I am testing as the "opal-test@hmcts.net" user

  @PO-313 @JIRA-KEY:POT-254
  Scenario: verifying the end points for API for Business Units Ref Data
    When I make a request to the business unit ref data api filtering by business unit type "area"
    Then the business unit ref data matching to result

  @PO-311 @JIRA-KEY:POT-255
  Scenario: Checking the end points for Offence ref data
    When I make a request to the offence ref data api filtering by cjs code "AA06"
    Then the offence ref data matching to result

#  @PO-315
#  Scenario: Checking the end points for court ref data1
#    When I make a request to the court ref data api with a filter of "Highbury"
#    Then the court ref data matching to result

  @PO-349 @JIRA-KEY:POT-256
  Scenario: Verifying the end points for the major creditors
    When I make a request to the major creditors ref data api filter by major creditor id 1300000000075
    Then the major creditors ref data matching to result

  @PO-312 @JIRA-KEY:POT-257
  Scenario: Checking the end points for LJA ref data
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @PO-316 @JIRA-KEY:POT-258
  Scenario: verifying the end points for enforcer ref data
    When I make a request to enforcer ref data api filtering by name "Alder"
    Then the enforcer ref data matching to result
