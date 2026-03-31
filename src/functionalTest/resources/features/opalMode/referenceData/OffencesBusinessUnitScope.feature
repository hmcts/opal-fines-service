@Opal @JIRA-LABEL:reference-data
Feature: Offences Business Unit Scope

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-4563
  Scenario: The Offences API returns all the Global offences
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering with the offence title "Attempt theft"
    Then the response contains the below offence data
      | offence_title    | Attempt theft |
      | business_unit_id | null          |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-4564
  Scenario: The Offences API returns all the Local offences
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 0
    Then the response contains the below offence data
      | business_unit_id | not null |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-4565
  Scenario: The Offences API returns filtered Local offences
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 1
    Then the response contains the below offence data
      | business_unit_id | 1 |
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response contains the below offence data
      | business_unit_id | 12 |

  @JIRA-STORY:PO-420 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-4566
  Scenario: The Offences API returns filtered Local offences - negative test
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response does not contain the below offence data
      | business_unit_id | 1 |
