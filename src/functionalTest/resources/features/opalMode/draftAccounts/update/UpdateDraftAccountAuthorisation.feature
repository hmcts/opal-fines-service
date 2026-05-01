@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:authorisation
Feature: Update Draft Account Authorisation

  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patch draft account - no auth
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
      | If-Match          | 0                                       |

    When I set an invalid token
    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the request is rejected as unauthorized


  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - user with no permissions
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
      | If-Match          | 0                                       |

    When the "opal-test-2@dev.platform.hmcts.net" user attempts to patch the created draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |


  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - user with permissions in different business unit - bu 73 to 26
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
      | If-Match          | 0                                       |

    When the "opal-test-3@dev.platform.hmcts.net" user attempts to patch the created draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |


  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - user with permissions in different business unit - bu 26 to 73
    Given I am testing as the "opal-test-3@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 26                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
      | version           | 0                                       |

    When the "opal-test@dev.platform.hmcts.net" user attempts to patch the created draft account with the following details
      | business_unit_id | 26                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the request is rejected as forbidden and the created draft account remains with the following data
      | business_unit_id                    | 26            |
      | account_type                        | Fine          |
      | account_status                      | Submitted     |
      | account_snapshot.defendant_name     | null, null    |
      | account_snapshot.date_of_birth      |               |
      | account_snapshot.account_type       | Fine          |
      | account_snapshot.business_unit_name | Hertfordshire |


  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - user with permissions in same business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                          |
      | account           | draftAccounts/accountJson/adultAccount.json |
      | account_type      | Fine                                        |
      | account_status    | Submitted                                   |
      | submitted_by      | BUUID                                       |
      | submitted_by_name | Laura Clerk                                 |
      | timeline_data     | draftAccounts/timelineJson/default.json     |

    When I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the created draft account is patched successfully and the retrieved draft account contains the following data
      | business_unit_id                    | 73           |
      | account_type                        | Fine         |
      | account_status                      | Rejected     |
      | account_snapshot.defendant_name     | LNAME, FNAME |
      | account_snapshot.date_of_birth      | 2000-01-01   |
      | account_snapshot.account_type       | Fine         |
      | account_snapshot.submitted_by       | L073JG       |
      | account_snapshot.business_unit_name | West London  |


  @JIRA-STORY:PO-831 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Update draft account - user with permissions in same business unit - updating business unit
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    When I patch the draft account with the following details
      | business_unit_id | 77                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |
    Then the request is rejected as conflict and the created draft account remains with the following data
      | business_unit_id                    | 73          |
      | account_type                        | Fine        |
      | account_status                      | Submitted   |
      | account_snapshot.defendant_name     | null, null  |
      | account_snapshot.date_of_birth      |             |
      | account_snapshot.account_type       | Fine        |
      | account_snapshot.submitted_by       | L073JG      |
      | account_snapshot.business_unit_name | West London |
