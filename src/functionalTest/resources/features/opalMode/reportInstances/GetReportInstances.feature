# Ignored because this feature is awaiting the DB changes for get report instances.
@Opal @Ignore @JIRA-LABEL:report-instances @R1CEnforcementOperationalReporting
Feature: Get Report Instances

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances with multiple filters applied
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request report instances with the following filters
      | report_id       | fp_register |
      | business_units  | 77          |
      | from_date       | 2026-05-10  |
      | to_date         | 2026-05-30  |
      | user_id         | 12345678    |
    Then the report instances response status is 200 OK
    And only report instances matching the following filters are returned
      | report_id       | fp_register |
      | business_units  | 77          |
      | from_date       | 2026-05-10  |
      | to_date         | 2026-05-30  |
      | user_id         | 12345678    |
    And the report instances response contains the following data
      | instance_id                        | 99000000008000 |
      | report_id                          | fp_register    |
      | business_units[0].business_unit_id | 77             |
      | requested_by.user_id               | 12345678       |
      | status.code                        | READY          |
    And instance with status READY and valid supported file types is marked downloadable

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances without a token is rejected
    And I attempt to retrieve the report instances without a token
    Then I get 401 when token missing or invalid

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances with an invalid token is rejected
    And I attempt to retrieve the report instances with an invalid token
    Then I get 401 when token missing or invalid

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances when user lacks permission in all relevant business units
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And get the report with report_id "fp_register"
    When send request to api with data which has lack of permissions
    Then I get 403 when token missing or invalid


  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances with multiple filters applied
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request report instances with the following filters
      | report_id       | fp_register |
      | business_units  | 77          |
      | from_date       | 2026-05-10  |
      | to_date         | 2026-05-30  |
      | user_id         | 12345678    |
    Then the report instances response status is 200 OK

  @JIRA-STORY:PO-2251 @JIRA-EPIC:PO-2248
  Scenario: Get report instances returns empty array when no instances match
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    When I request report instances with the following filters
      | report_id       | fp_register |
      | business_units  | 77          |
      | from_date       | 2099-01-01  |
      | to_date         | 2099-01-31  |
      | user_id         | 12345678    |
    Then 200 for no matching report instances with an empty array.
