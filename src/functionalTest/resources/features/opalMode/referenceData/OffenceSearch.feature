@Opal @JIRA-LABEL:reference-data @JIRA-STORY:PO-926 @JIRA-EPIC:PO-304
Feature: Offence Search

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  Scenario: Offences can be searched by CJS code
    When I make a request to the offence search api filtering by
      | cjs_code    | TH68002 |
      | title       |         |
      | act_section |         |
      | active_date |         |
      | max_results |         |

    Then the request succeeds
    And the response contains results with a cjs code starting with "TH68002"
    And the offences in the response contain the following data
      | cjs_code      | TH68002     |
      | offence_title | in dwelling |


  Scenario: Offences can be searched by title
    When I make a request to the offence search api filtering by
      | cjs_code    |                   |
      | title       | in dwelling other |
      | act_section |                   |
      | active_date |                   |
      | max_results |                   |

    Then the request succeeds
    And the offences in the response contain the following data
      | offence_title | in dwelling other |


  Scenario: Offences can be searched by act and section
    When I make a request to the offence search api filtering by
      | cjs_code        |                                                                                  |
      | title           |                                                                                  |
      | act_and_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date     |                                                                                  |
      | max_results     |                                                                                  |

    Then the request succeeds
    And the offences in the response contain the following data
      | offence_oas | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |


  Scenario: Offences can be searched using multiple filters
    When I make a request to the offence search api filtering by
      | cjs_code        | AV9                                                                              |
      | title           | PERSONAL INJURY and endangering safe operation                                   |
      | act_and_section | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |
      | active_date     |                                                                                  |
      | max_results     |                                                                                  |

    Then the request succeeds
    And the offences in the response contain the following data
      | cjs_code      | AV9                                                                              |
      | offence_title | personal injury and endangering safe operation                                   |
      | offence_oas   | Contrary to section 1(1) and (5) of the Aviation and Maritime Security Act 1990. |


  Scenario: Max results limits the number of returned offences
    When I make a request to the offence search api filtering by
      | cjs_code        |   |
      | title           |   |
      | act_and_section |   |
      | active_date     |   |
      | max_results     | 2 |

    Then the request succeeds
    And there are 2 offences in the response

    When I make a request to the offence search api filtering by
      | cjs_code        |    |
      | title           |    |
      | act_and_section |    |
      | active_date     |    |
      | max_results     | 20 |

    Then the request succeeds
    And there are 20 offences in the response


  Scenario: Active date filters offences by date used
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101               |
      | title           |                      |
      | act_and_section |                      |
      | active_date     | 1920-03-12T00:00:00Z |
      | max_results     | 100                  |

    Then the request succeeds
    And the offences in the response are before "1920-03-12T00:00:00Z" only


  Scenario: Inactive offences are returned when no active date is supplied
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101 |
      | title           |        |
      | act_and_section |        |
      | active_date     |        |
      | max_results     | 100    |
    Then the request succeeds
    And there are 3 offences in the response


  Scenario: Inactive offences are excluded when an active date is supplied
    When I make a request to the offence search api filtering by
      | cjs_code        | PA1101               |
      | title           |                      |
      | act_and_section |                      |
      | active_date     | 2024-03-12T00:00:00Z |
      | max_results     | 100                  |
    Then the request succeeds
    And there are 0 offences in the response


  Scenario: No offences are returned when the filters do not match
    When I make a request to the offence search api filtering by
      | cjs_code        | AB12345          |
      | title           | Offence not real |
      | act_and_section |                  |
      | active_date     |                  |
      | max_results     | 10               |
    Then the request succeeds
    And there are 0 offences in the response
