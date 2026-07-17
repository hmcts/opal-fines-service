@Opal @JIRA-LABEL:reference-data @R1CReferenceData
Feature: Report Definition Reference Data

  # NOTE: E2E.01 cannot currently be covered in QA from this service alone.
  # The seeded reports data returns 403 because the available report rows have permission = NULL,
  @Ignore @JIRA-STORY:PO-2250 @JIRA-EPIC:PO-2248
  Scenario: Report definition can be retrieved with a valid token
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I make a request to the configured report definition api
    Then the report definition matches the documented contract

  @JIRA-STORY:PO-2250 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7859
  Scenario: Report definition requests without a token are rejected by the security layer
    When I call GET on the seeded report definition api with "no token"
    Then the request is rejected with status 401
    And the latest report definition response is an unauthorized response

  @JIRA-STORY:PO-2250 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7860
  Scenario: Report definition requests with an invalid token are rejected with standard error responses
    When I call GET on the seeded report definition api with "invalid token"
    Then the request is rejected with status 401
    And the latest report definition error response matches the standard problem detail contract for status 401

  @JIRA-STORY:PO-2250 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7861
  Scenario: Seeded report definition forbidden responses match the standard error contract
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I make a request to the seeded report definition api
    Then the request is rejected as forbidden
    And the latest report definition error response matches the standard problem detail contract for status 403
