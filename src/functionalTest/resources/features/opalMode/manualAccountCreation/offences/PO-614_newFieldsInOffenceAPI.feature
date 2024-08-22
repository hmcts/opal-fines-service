Feature: PO-614 Introduce the new OFFENCE table columns into the OFFENCES entity

  @Opal @PO-614
  Scenario: Checking additional columns are presented in the Offence API
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence ref data api filtering by cjs code "AA06001"
    Then the response contains the below offence data fields and values
      | offenceId         | 310844                                                                                                      |
      | getCjsCode        | AA06001                                                                                                     |
      | businessUnitId    | null                                                                                                        |
      | getOffenceTitle   | Fail to wear protective clothing/footwear and meets other criteria when entering quarantine centre/facility |
      | getOffenceTitleCy | null                                                                                                        |
      | dateUsedFrom      | 2007-06-30T00:00:00                                                                                         |
      | dateUsedTo        | 2007-06-30T00:00:00                                                                                         |
      | offenceOas        | null                                                                                                        |
      | offenceOasCy      | null                                                                                                        |
