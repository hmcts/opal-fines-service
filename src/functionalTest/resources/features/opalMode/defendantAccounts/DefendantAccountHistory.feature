@Opal @JIRA-LABEL:account-enquiry
Feature: Defendant Account History

#  Temporarily Commented Until Data Is Added to Staging
#  @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621
#  Scenario: E2E.01 Happy path history retrieval
#    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
#    When I request defendant account history for account 99000000000001
#    Then the defendant account history response is returned as documented
#    And the defendant account history contains exactly the following item counts
#      | Enforcement   | 1 |
#      | Financial     | 1 |
#      | Note          | 1 |
#      | Payment terms | 1 |
#    And the defendant account history contains seeded enforcement history
#    And the defendant account history is ordered newest first
#
#  @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-2621
#  Scenario: E2E.02 Filter contract and idempotence
#    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
#    When I request defendant account history for account 99000000000001 with query "dateFrom=2026-05-12"
#    Then the defendant account history response contains only items on or after "2026-05-12"
#    And the defendant account history response includes an item on "2026-05-12"
#    When I request defendant account history for account 99000000000001 with query "dateTo=2026-05-11"
#    Then the defendant account history response contains only items on or before "2026-05-11"
#    And the defendant account history response includes an item on "2026-05-11"
#    When I request defendant account history for account 99000000000001 with query "itemTypes=ENFORCEMENT"
#    Then the defendant account history contains only the following item types
#      | Enforcement |
#    And the defendant account history contains seeded enforcement history
#    When I request defendant account history for account 99000000000001 with query "itemTypes=enforcement,note"
#    Then the defendant account history contains only the following item types
#      | Enforcement |
#      | Note        |
#    When I request defendant account history for account 99000000000001 with query "dateFrom=2026-05-11&dateTo=2026-05-12&itemTypes=enforcement,note"
#    Then the defendant account history contains only the following item types
#      | Enforcement |
#      | Note        |
#    And the defendant account history response contains only items on or after "2026-05-11"
#    And the defendant account history response contains only items on or before "2026-05-12"
#    When I request defendant account history for account 99000000000001 with query "dateFrom=2026-05-11&dateTo=2026-05-12&itemTypes=enforcement,note" twice
#    Then the repeated defendant account history responses are identical
#
#  @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-812 @JIRA-NFR:PO-2507
#  Scenario: E2E.03 Authentication and authorization
#    When I request defendant account history for account 99000000000001 without a token
#    Then the defendant account history error response matches the standard problem detail contract for status 401
#    When the "opal-test-2@dev.platform.hmcts.net" user requests defendant account history for account 99000000000001
#    Then the defendant account history error response matches the standard problem detail contract for status 403
#    When the "opal-test@dev.platform.hmcts.net" user requests defendant account history for account 99000000000001
#    Then the defendant account history request succeeds

  @JIRA-STORY:PO-2622 @JIRA-EPIC:PO-812 @JIRA-TEST-KEY:PO-7854
  Scenario: E2E.04 Not found behaviour
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request defendant account history for account 99999999999999
    Then the defendant account history error response matches the standard problem detail contract for status 404
    And the defendant account history error response does not leak internal details
