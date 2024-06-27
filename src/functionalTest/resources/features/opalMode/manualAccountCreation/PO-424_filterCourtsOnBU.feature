Feature: Courts API can be filtered by business unit id

  @Opal @PO-424
  Scenario: I can filter the court ref data api by name and business unit id
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with a filter of "magistrates" and a business unit of 43
    Then the response contains the below courts data
      | name           | magistrates |
      | businessUnitId | 43          |

  @Opal @PO-424
  Scenario: I can filter the court ref data api by name and business unit id - negative test
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with a filter of "result" and a business unit of 48
    Then the response does not contain the below courts data
      | name           | magistrates |
      | businessUnitId | 43          |

  @Opal @PO-424
  Scenario: I can filter the court ref data api by just business unit id
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with a business unit of 43
    Then the response contains the below courts data
      | businessUnitId | 43 |

  @Opal @PO-424
  Scenario: I can filter the court ref data api by just business unit id - negative test
    Given I am testing as the "opal-test@hmcts.net" user
    When I make a request to the court ref data api with a business unit of 84
    Then the response does not contain the below courts data
      | businessUnitId | 43 |
