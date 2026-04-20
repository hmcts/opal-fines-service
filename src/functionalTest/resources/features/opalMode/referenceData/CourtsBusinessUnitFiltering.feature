@Opal @JIRA-LABEL:reference-data
Feature: Courts Business Unit Filtering

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6187
  Scenario: I can filter the court ref data api by name and business unit id
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a filter of "magistrates" and a business unit of 43
    Then the response contains the below courts data
      | name             | magistrates |
      | business_unit_id | 43          |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6189
  Scenario: I can filter the court ref data api by name and business unit id - negative test
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a filter of "result" and a business unit of 48
    Then the response does not contain the below courts data
      | name             | magistrates |
      | business_unit_id | 43          |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6191
  Scenario: I can filter the court ref data api by just business unit id
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a business unit of 43
    Then the response contains the below courts data
      | business_unit_id | 43 |

  @JIRA-STORY:PO-424 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6193
  Scenario: I can filter the court ref data api by just business unit id - negative test
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the court ref data api with a business unit of 84
    Then the response does not contain the below courts data
      | business_unit_id | 43 |
