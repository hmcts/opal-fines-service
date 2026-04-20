@Opal @JIRA-LABEL:reference-data
Feature: Local Justice Areas Reference Data

  @JIRA-STORY:PO-312 @JIRA-EPIC:PO-304
 #PO-312

  @JIRA-KEY:POT-6195
  Scenario: Checking the local justice areas reference data endpoint
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6197
  Scenario: Happy path without lja_type returns all LJAs
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the LJA ref data api with
    Then the response status code is 200

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6199
  Scenario: Deployed filtering with single lja_type
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the LJA ref data api with lja_type "LJA"
    Then the response status code is 200
    And all returned LJAs have lja_type "LJA"

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-KEY:POT-6200
  Scenario: Deployed filtering with multiple lja_type values
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the LJA ref data api with lja_type "LJA,CRWCRT"
    Then the response status code is 200
    And all returned LJAs have lja_type in "LJA,CRWCRT"




