@Opal @JIRA-LABEL:manual-account-creation @JIRA-LABEL:error-handling
Feature: Update Draft Account Error Handling

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a draft account with invalid data is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    When I patch the draft account with the following details
      | business_unit_id__ | 73                   |
      | account_status     | Rejected             |
      | validated_by       | BUUID_REVIEWER       |
      | reason_text        | Reason for rejection |
      | If-Match           | 0                    |
    Then the request is rejected as bad request

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a draft account without a valid access token is rejected
    Given a draft account exists with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |

    When I set an invalid token
    And I patch the draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |
    Then the request is rejected as unauthorized

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a missing draft account is rejected
    When I patch the "1000000000" draft account with the following details
      | business_unit_id | 73                   |
      | account_status   | Rejected             |
      | validated_by     | BUUID_REVIEWER       |
      | reason_text      | Reason for rejection |
      | If-Match         | 0                    |

    Then the request is rejected as not found

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a draft account with an unsupported response content type is rejected
    When I attempt to patch a draft account with an unsupported content type
    Then the request is rejected as not acceptable

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a draft account with an unsupported request media type is rejected
    When I attempt to patch a draft account with an unsupported media type
    Then the request is rejected as unsupported media type

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Patching a draft account with a malformed request fails
    When I patch the draft account trying to provoke an internal server error
    Then the request fails with an internal server error

  @JIRA-STORY:PO-747 @JIRA-EPIC:PO-2220 @cleanUpData
  Scenario: Reusing a stale ETag when patching a draft account is rejected as conflict
    When I create a draft account with the following details
      | business_unit_id  | 73                                      |
      | account           | draftAccounts/accountJson/account.json  |
      | account_type      | Fine                                    |
      | account_status    | Submitted                               |
      | submitted_by      | BUUID                                   |
      | submitted_by_name | Laura Clerk                             |
      | timeline_data     | draftAccounts/timelineJson/default.json |
    Then the request creates a resource
    And the response must include a strong quoted ETag header
    And I remember the last response ETag as "before"

    When I patch the draft account with the following details
      | business_unit_id | 73                |
      | account_status   | Rejected          |
      | validated_by     | BUUID_REVIEWER    |
      | reason_text      | Reason for change |
      | If-Match         | $etag:before      |
    Then the request succeeds
    And the response must include a strong quoted ETag header

    # First PATCH uses the "before" ETag → succeeds and increments version
    When I patch the draft account with the following details
      | business_unit_id | 73             |
      | account_status   | Deleted        |
      | validated_by     | BUUID_REVIEWER |
      | reason_text      | Revert         |
      | If-Match         | $etag:before   |
    # Second PATCH reuses the stale "before" eTag again → 409 Conflict
    Then the request is rejected as conflict
