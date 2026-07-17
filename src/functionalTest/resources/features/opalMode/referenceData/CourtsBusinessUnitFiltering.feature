@Opal @JIRA-LABEL:reference-data @R1CReferenceData
Feature: Courts Business Unit Filtering

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5707
  Scenario: Courts can be filtered by name within a business unit
    When I make a request to the court ref data api with a filter of "magistrates" and a business unit of 43
    Then the response contains the below courts data
      | name             | magistrates |
      | business_unit_id | 43          |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5708
  Scenario: Courts outside the requested business unit are excluded from a name search
    When I make a request to the court ref data api with a filter of "result" and a business unit of 48
    Then the response does not contain the below courts data
      | name             | magistrates |
      | business_unit_id | 43          |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-TEST-KEY:PO-5709
  Scenario: Courts can be filtered by business unit alone
    When I make a request to the court ref data api with a business unit of 43
    Then the response contains the below courts data
      | business_unit_id | 43 |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-NFR:PO-2507 @JIRA-TEST-KEY:PO-5710
  Scenario: Courts from other business units are excluded
    When I make a request to the court ref data api with a business unit of 84
    Then the response does not contain the below courts data
      | business_unit_id | 43 |
