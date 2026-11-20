@Opal @ExtendedTest @JIRA-LABEL:reference-data @R1CReferenceData
Feature: Local Justice Areas Reference Data

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-312 @JIRA-EPIC:PO-304

  @JIRA-TEST-KEY:PO-5711
  Scenario: Local justice areas reference data can be retrieved
    When I make a request to the LJA ref data api with
    Then the LJA ref data matching to result

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-TEST-KEY:PO-5712
  Scenario: All local justice areas are returned when no type is supplied
    When I make a request to the LJA ref data api with
    Then the request succeeds

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-TEST-KEY:PO-5713
  Scenario: Local justice areas can be filtered by a single type
    When I make a request to the LJA ref data api with lja_type "LJA"
    Then the request succeeds
    And all returned LJAs have lja_type "LJA"

  @JIRA-STORY:PO-2757 @JIRA-EPIC:PO-2750 @JIRA-TEST-KEY:PO-5714
  Scenario: Local justice areas can be filtered by multiple types
    When I make a request to the LJA ref data api with lja_type "LJA,CRWCRT"
    Then the request succeeds
    And all returned LJAs have lja_type in "LJA,CRWCRT"

  @LjaMessageConsumption @JIRA-STORY:PO-10823 @JIRA-EPIC:PO-9031
  Scenario: Create an LJA from the JSON fixture
    Given a new LJA request is loaded from the publication JSON fixture
    When I POST the LJA request to the reference data topic
    Then the LJA publication response status is 202
    And the LJA GET response contains the expected values

  @LjaMessageConsumption @JIRA-STORY:PO-10823 @JIRA-EPIC:PO-9031
  Scenario: Update an LJA created by this scenario
    Given an LJA from the publication JSON fixture has been created and verified
    When I POST these changes to the same LJA
      | field          | value                 |
      | lja_name       | <unique:updated:name> |
      | address_line_1 | Updated Court Address |
    Then the LJA publication response status is 202
    And the LJA GET response contains the expected values

  @LjaMessageConsumption @JIRA-STORY:PO-10823 @JIRA-EPIC:PO-9031
  Scenario: A supplied LJA end date is ignored
    Given an LJA from the publication JSON fixture has been created and verified
    When I POST these changes to the same LJA
      | field    | value               |
      | lja_name | <unique:dated:name> |
      | end_date | 2027-03-10          |
    Then the LJA publication response status is 202
    And the LJA GET response contains the expected values
    And the LJA GET response end date is null

  @LjaMessageConsumption @JIRA-STORY:PO-10823 @JIRA-EPIC:PO-9031
  Scenario: Reject an LJA request with a null name
    Given a new LJA request is loaded from the publication JSON fixture
    When I POST the LJA request with lja_name set to null
    Then the LJA publication response status is 400
    And no LJAs exist for "owned"
