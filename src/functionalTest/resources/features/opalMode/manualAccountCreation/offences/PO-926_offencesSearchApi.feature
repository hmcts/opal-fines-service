@Opal @PO-926
Feature: PO-926 Offences Search API

  @JIRA-KEY:POT-245
  Scenario: Offence Search API - Search by CJS code
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code    | TH68002 |
      | title       |         |
      | act_section |         |
      | active_date |         |
      | max_results |         |

    Then The offence search response returns 200
    And the response contains results with a cjs code starting with "TH68002"
    And the offences in the response contain the following data
      | cjs_code      | TH68002     |
      | offence_title | in dwelling |

  @JIRA-KEY:POT-246
  Scenario: Offence Search API - Search Title
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code    |                   |
      | title       | in dwelling other |
      | act_section |                   |
      | active_date |                   |
      | max_results |                   |

    Then The offence search response returns 200
    And the offences in the response contain the following data
      | offence_title | in dwelling other |

  @JIRA-KEY:POT-247
  Scenario: Offence Search API - Search by Act and Section
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        |                                                                                  |
      | title           |                                                                                  |
      | act_and_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date     |                                                                                  |
      | max_results     |                                                                                  |

    Then The offence search response returns 200
    And the offences in the response contain the following data
      | offence_oas | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |

  @JIRA-KEY:POT-248
  Scenario: Offence Search API - Search by all fields
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        | AV9                                                                              |
      | title           | PERSONAL INJURY and endangering safe operation                                   |
      | act_and_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date     |                                                                                  |
      | max_results     |                                                                                  |

    Then The offence search response returns 200
    And the offences in the response contain the following data
      | cjs_code      | AV9                                                                              |
      | offence_title | personal injury and endangering safe operation                                   |
      | offence_oas   | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |

  @JIRA-KEY:POT-249
  Scenario: Offence Search API - Max results
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        |   |
      | title           |   |
      | act_and_section |   |
      | active_date     |   |
      | max_results     | 2 |

    Then The offence search response returns 200
    And there are 2 offences in the response

    When I make a request to the offence search api filtering by
      | cjs_code        |    |
      | title           |    |
      | act_and_section |    |
      | active_date     |    |
      | max_results     | 20 |

    Then The offence search response returns 200
    And there are 20 offences in the response

  @JIRA-KEY:POT-250
  Scenario: Offence Search API - Search by Active Date
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101               |
      | title           |                      |
      | act_and_section |                      |
      | active_date     | 1920-03-12T00:00:00Z |
      | max_results     | 100                  |

    Then The offence search response returns 200
    Then the offences in the response are before "1920-03-12T00:00:00Z" only

  @JIRA-KEY:POT-251
  Scenario: Offence Search API - Inactive Offences - Active Date Null - Inactive offences returned
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101 |
      | title           |        |
      | act_and_section |        |
      | active_date     |        |
      | max_results     | 100    |
    Then The offence search response returns 200
    And there are 3 offences in the response

  @JIRA-KEY:POT-252
  Scenario: Offence Search API - Inactive Offences - Active Date populated - Inactive offences not returned
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101               |
      | title           |                      |
      | act_and_section |                      |
      | active_date     | 2024-03-12T00:00:00Z |
      | max_results     | 100                  |
    Then The offence search response returns 200
    And there are 0 offences in the response

  @JIRA-KEY:POT-253
  Scenario: Offence Search API - No Results
      ### need to test no results are returned when no offences match the search criteria and the status is 200
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code        | AB12345          |
      | title           | Offence not real |
      | act_and_section |                  |
      | active_date     |                  |
      | max_results     | 10               |
    Then The offence search response returns 200
    And there are 0 offences in the response
