@Opal @JIRA-LABEL:reference-data
Feature: Offence Additional Fields

  @JIRA-STORY:PO-614 @JIRA-EPIC:PO-304 @JIRA-KEY:POT-6208
  Scenario: Checking additional columns are presented in the Offence API
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the offence ref data api filtering by cjs code "AA06001"
    Then the response contains the below offence data fields and values
      | offence_id       | 45010                                                                                                       |
      | cjs_code         | AA06001                                                                                                     |
      | business_unit_id | null                                                                                                        |
      | offence_title    | Fail to wear protective clothing/footwear and meets other criteria when entering quarantine centre/facility |
      | offence_title_cy | null                                                                                                        |
      | date_used_from   | 2006-11-01T00:00:00Z                                                                                        |
      | date_used_to     | 2007-06-30T00:00:00Z                                                                                        |
      | offence_oas      | null                                                                                                        |
      | offence_oas_cy   | null                                                                                                        |
