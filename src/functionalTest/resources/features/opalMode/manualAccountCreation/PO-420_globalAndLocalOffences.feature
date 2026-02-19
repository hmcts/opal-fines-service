Feature: PO-420 Global and Local Offences

  @Opal @PO-420 @JIRA-KEY:POT-160
  Scenario: The Offences API returns all the Global offences
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering with the offence title "Attempt theft"
    Then the response contains the below offence data
      | offence_title    | Attempt theft |
      | business_unit_id | null          |

  @Opal @PO-420 @JIRA-KEY:POT-161
  Scenario: The Offences API returns all the Local offences
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 0
    Then the response contains the below offence data
      | business_unit_id | not null |

  @Opal @PO-420 @JIRA-KEY:POT-162
  Scenario: The Offences API returns filtered Local offences
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 1
    Then the response contains the below offence data
      | business_unit_id | 1 |
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response contains the below offence data
      | business_unit_id | 12 |

  @Opal @PO-420 @JIRA-KEY:POT-163
  Scenario: The Offences API returns filtered Local offences - negative test
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering by business unit 12
    Then the response does not contain the below offence data
      | business_unit_id | 1 |
