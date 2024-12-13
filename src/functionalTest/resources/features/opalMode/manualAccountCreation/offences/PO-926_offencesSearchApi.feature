@Opal @PO-926
Feature: PO-926 Offences Search API
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

  Scenario: Offence Search API - Search by Act and Section
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code    |                                                                                  |
      | title       |                                                                                  |
      | act_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date |                                                                                  |
      | max_results |                                                                                  |

    Then The offence search response returns 200
    And the offences in the response contain the following data
      | offence_oas | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |

  Scenario: Offence Search API - Search by all fields
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code    | AV9                                                                              |
      | title       | PERSONAL INJURY and endangering safe operation                                   |
      | act_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date |                                                                                  |
      | max_results |                                                                                  |

    Then The offence search response returns 200
    And the offences in the response contain the following data
      | cjs_code      | AV9                                                                              |
      | offence_title | personal injury and endangering safe operation                                   |
      | offence_oas   | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |

  Scenario: Offence Search API - Max results
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the offence search api filtering by
      | cjs_code    |   |
      | title       |   |
      | act_section |   |
      | active_date |   |
      | max_results | 2 |

    Then The offence search response returns 200
    And there are 2 offences in the response

    When I make a request to the offence search api filtering by
      | cjs_code    |    |
      | title       |    |
      | act_section |    |
      | active_date |    |
      | max_results | 20 |

    Then The offence search response returns 200
    And there are 20 offences in the response

  Scenario: Offence Search API - Search by Active Date
      ### need to test only dates before active date are returned

  Scenario: Offence Search API - Inactive Offences
      ### need to test inactive offences are returned when active date is null

  Scenario: Offence Search API - No Results
      ### need to test no results are returned when no offences match the search criteria and the status is 200
