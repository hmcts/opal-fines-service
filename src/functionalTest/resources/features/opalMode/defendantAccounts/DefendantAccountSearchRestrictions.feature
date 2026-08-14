@Opal @R1B @JIRA-LABEL:account-enquiry @JIRA-EPIC:PO-2630 @JIRA-STORY:PO-2970
Feature: Defendant Account Search API restrictions

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  Scenario Outline: AC1 - National insurance number cannot be combined with other defendant search fields
    When I search defendant accounts with national insurance number and "<conflicting_field>" populated
    Then the defendant account search request is rejected as a schema bad request

    @JIRA-TEST-KEY:PO-10043
    Examples:
      | conflicting_field |
      | address_line_1    |
    @JIRA-TEST-KEY:PO-10044
    Examples:
      | conflicting_field |
      | postcode          |
    @JIRA-TEST-KEY:PO-10045
    Examples:
      | conflicting_field |
      | organisation_name |
    @JIRA-TEST-KEY:PO-10046
    Examples:
      | conflicting_field |
      | surname           |
    @JIRA-TEST-KEY:PO-10047
    Examples:
      | conflicting_field |
      | forenames         |
    @JIRA-TEST-KEY:PO-10048
    Examples:
      | conflicting_field |
      | birth_date        |

  @JIRA-TEST-KEY:PO-10049
  Scenario: AC3 - First name without last name returns bad request
    When I search defendant accounts with only "forenames" populated
    Then the defendant account search request is rejected as a schema bad request

  @JIRA-TEST-KEY:PO-10050
  Scenario: AC4 - First name with last name returns the created account
    Given a published defendant account exists for PO-2970 defendant account search
    When I search for the PO-2970 account using the allowed payloads
      | forenames_and_surname |
    Then each allowed defendant search returns the created account

  @JIRA-TEST-KEY:PO-10051
  Scenario: AC5 - Date of birth without last name returns bad request
    When I search defendant accounts with only "birth_date" populated
    Then the defendant account search request is rejected as a schema bad request

  @JIRA-TEST-KEY:PO-10052
  Scenario: AC2 - National insurance number without other fields returns the created account
    Given a published defendant account exists for PO-2970 defendant account search
    When I search for the PO-2970 account using the allowed payloads
      | national_insurance_number |
    Then each allowed defendant search returns the created account

  @JIRA-TEST-KEY:PO-10053
  Scenario: AC6 - Date of birth with last name returns the created account
    Given a published defendant account exists for PO-2970 defendant account search
    When I search for the PO-2970 account using the allowed payloads
      | birth_date_and_surname |
    Then each allowed defendant search returns the created account

  @JIRA-TEST-KEY:PO-10054
  Scenario: AC7 - Address line 1 without last name or organisation returns the created account
    Given a published defendant account exists for PO-2970 defendant account search
    When I search for the PO-2970 account using the allowed payloads
      | address_line_1_only |
    Then each allowed defendant search returns the created account

  @JIRA-TEST-KEY:PO-10055
  Scenario: AC8 - Postcode without last name or organisation returns the created account
    Given a published defendant account exists for PO-2970 defendant account search
    When I search for the PO-2970 account using the allowed payloads
      | postcode_only |
    Then each allowed defendant search returns the created account

  @JIRA-TEST-KEY:PO-10056
  Scenario: AC9 - Surname search only returns accounts where the surname starts with the search value
    Given published defendant accounts exist for PO-2970 surname starts-with search
    When I search defendant accounts by the PO-2970 surname prefix
    Then the defendant account search returns the PO-2970 starts-with account only
