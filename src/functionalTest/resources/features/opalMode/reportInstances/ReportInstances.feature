@Opal @JIRA-LABEL:report-instances @JIRA-LABEL:authorisation
Feature: Report Instances

  #    POST :/reports-instances/{id} Test scenarios

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

#    GET :/reports-instances/{id} Test scenarios

  @JIRA-STORY:PO-2254 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-8295
  Scenario: Get report instance returns complete instance details
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business unit 73
    Then I store the created report instance id
    When I request the created report instance
    Then the response status code is 200
    And the report instance response contains the created instance id
    And the report instance response contains core instance details

  @JIRA-STORY:PO-2254 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-8296
  Scenario: Get report instance without a token is rejected by the security layer
    When I call GET on the report instance api for id 1 with "no token"
    Then the request is rejected with status 401
    And the latest get report instance response is an unauthorized response

  @JIRA-STORY:PO-2254 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-8297
  Scenario: Get report instance with an invalid token is rejected with standard error responses
    When I call GET on the report instance api for id 1 with "invalid token"
    Then the request is rejected with status 401
    And the latest get report instance error response matches the standard problem detail contract for status 401

  @JIRA-STORY:PO-2254 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-8298
  Scenario: Get report instance without permission in the requested business unit is rejected as forbidden
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business unit 73
    Then I store the created report instance id
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request the created report instance
    Then the request is rejected as forbidden
    And the latest get report instance error response matches the standard problem detail contract for status 403

  @JIRA-STORY:PO-2254 @JIRA-EPIC:PO-2248 @JIRA-TEST-KEY:PO-8299
  Scenario: Get report instance with an unknown id is rejected as not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request report instance with id -1
    Then the request is rejected as not found
    And the latest get report instance error response matches the standard problem detail contract for status 404

   #    GET :/report-instances/{id}/content Test scenarios

  @JIRA-STORY:PO-2253 @JIRA-EPIC:PO-2248
  Scenario: Get report instance content with an unknown id is rejected as not found
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request report instance content with id -1
    Then the request is rejected as not found
    And the latest get report instance content error response matches the standard problem detail contract for status 404

  @JIRA-STORY:PO-2253 @JIRA-EPIC:PO-2248
  Scenario: Get report instance content without a token is rejected by the security layer
    When I call GET on the report instance content api for id 1 with "no token"
    Then the request is rejected with status 401
    And the latest get report instance content response is an unauthorized response

  @JIRA-STORY:PO-2253 @JIRA-EPIC:PO-2248
  Scenario: Get report instance content with an invalid token is rejected with standard error responses
    When I call GET on the report instance content api for id 1 with "invalid token"
    Then the request is rejected with status 401
    And the latest get report instance content error response matches the standard problem detail contract for status 401

  @JIRA-STORY:PO-2253 @JIRA-EPIC:PO-2248
  Scenario: Get report instance content without permission in the requested business unit is rejected as forbidden
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I create a report instance with report id "operational_report_enforcement" for business unit 73
    Then I store the created report instance id
    Given I am testing as the "opal-test-2@dev.platform.hmcts.net" user
    When I request the created report instance content
    Then the request is rejected as forbidden
    And the latest get report instance content error response matches the standard problem detail contract for status 403