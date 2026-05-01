@Opal @JIRA-LABEL:reference-data
Feature: Offences Business Unit Scope

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304
  Scenario: Global offences are returned without a business unit filter
    When I make a request to the offence ref data api filtering with the offence title "Attempt theft"
    Then the response contains the below offence data
      | offence_title    | Attempt theft |
      | business_unit_id | null          |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304
  Scenario: Local offences can be retrieved across business units
    When I make a request to the offence ref data api filtering by business unit 0
    Then the response contains the below offence data
      | business_unit_id | not null |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304
  Scenario: Local offences can be filtered to a specific business unit
    When I make a request to the offence ref data api filtering by business unit 1
    Then the response contains the below offence data
      | business_unit_id | 1 |
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response contains the below offence data
      | business_unit_id | 12 |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304
  Scenario: Offences from other business units are excluded
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response does not contain the below offence data
      | business_unit_id | 1 |
