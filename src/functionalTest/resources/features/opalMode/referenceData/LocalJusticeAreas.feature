@Opal @JIRA-LABEL:reference-data
Feature: Local Justice Areas Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-312 @JIRA-EPIC:PO-304

  @JIRA-KEY:POT-6195
  Scenario: Local justice areas reference data can be retrieved
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6197
  Scenario: All local justice areas are returned when no type is supplied
    When I make a request to the LJA ref data api with
    Then the request succeeds

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6199
  Scenario: Local justice areas can be filtered by a single type
    When I make a request to the LJA ref data api with lja_type "LJA"
    Then the request succeeds
    And all returned LJAs have lja_type "LJA"

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6200
  Scenario: Local justice areas can be filtered by multiple types
    When I make a request to the LJA ref data api with lja_type "LJA,CRWCRT"
    Then the request succeeds
    And all returned LJAs have lja_type in "LJA,CRWCRT"
