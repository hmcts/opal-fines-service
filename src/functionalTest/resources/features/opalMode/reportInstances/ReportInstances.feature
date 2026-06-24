@Opal @JIRA-LABEL:report-instances @JIRA-LABEL:authorisation
Feature: Report Instances

  # This verifies the create response only. Full created-instance verification needs PO-2254.
  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7862
  Scenario: Create report instance with a single business unit returns 201 and a report instance id
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business unit 73
    Then the request creates a resource
    And the report instance create response contains a report instance id

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7863
  Scenario: Create report instance without a token is rejected by the security layer
    When I call POST on the report instances api with "no token"
    Then the request is rejected with status 401
    And the latest report instance create response is an unauthorized response

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7864
  Scenario: Create report instance with an invalid token is rejected with standard error responses
    When I call POST on the report instances api with "invalid token"
    Then the request is rejected with status 401
    And latest report instance create error response matches the standard problem detail contract for status 401

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7865
  Scenario: Create report instance with an unknown report id is rejected as not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "report-id-that-does-not-exist"
    Then the request is rejected as not found
    And latest report instance create error response matches the standard problem detail contract for status 404

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7866
  Scenario: Create report instance without permission in the requested business unit is rejected as forbidden
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement"
    Then the request is rejected as forbidden
    And latest report instance create error response matches the standard problem detail contract for status 403

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7867
  Scenario: Create report instance for a report that cannot be manually created is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "fp_register"
    Then the request is rejected with status 422
    And latest report instance create error response matches the standard problem detail contract for status 422

  @JIRA-STORY:PO-2252 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-7868
  Scenario: Create report instance with multiple business units for a single-BU report is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business units 73 and 1001
    Then the request is rejected with status 422
    And latest report instance create error response matches the standard problem detail contract for status 422

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Create report instance with multiple business units for a single-BU report is rejected
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business units 73 and 1001
    Then the request is rejected with status 422
    And latest report instance create error response matches the standard problem detail contract for status 422


