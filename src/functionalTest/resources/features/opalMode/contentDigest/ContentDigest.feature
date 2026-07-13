@Opal @JIRA-LABEL:content-digest @JIRA-STORY:PO-2878 @JIRA-EPIC:PO-2675
Feature: Content-Digest handling

  @JIRA-TEST-KEY:PO-5776
  Scenario: Missing request Content-Digest succeeds when content digest is disabled
    When I make a content digest request without a Content-Digest header
    Then The content digest response returns 200
    And The content digest response does not contain a Content-Digest header

  @JIRA-TEST-KEY:PO-5777
  Scenario: Valid request Content-Digest succeeds
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a content digest request with a valid Content-Digest header
    Then The content digest response returns 200

  @JIRA-TEST-KEY:PO-5778
  Scenario: Invalid request Content-Digest returns a problem response
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a content digest request with an invalid Content-Digest header
    Then The content digest response returns 400
    And The content digest response contains the following
      | title  | Digest validation failed                      |
      | detail | Body hash did not match for algorithm: sha-512 |

  @JIRA-TEST-KEY:PO-5779
  Scenario: Malformed request Content-Digest returns a problem response
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a content digest request with a malformed Content-Digest header
    Then The content digest response returns 400
    And The content digest response contains the following
      | title  | Invalid Content-Digest header          |
      | detail | No valid digest entries found in header. |
