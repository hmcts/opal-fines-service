@Opal @R1B @JIRA-LABEL:account-enquiry @JIRA-EPIC:PO-2630 @JIRA-STORY:PO-2971 @MinorCreditorSearch
Feature: Minor Creditor Account Search API restrictions

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user
    And a minor creditor account exists for PO-2971 minor creditor account search

  Scenario Outline: AC1 - Account number cannot be combined with creditor search fields
    When I search minor creditor accounts with the created account number and "<conflicting_field>" populated
    Then the minor creditor account search request is rejected as a schema bad request

    @JIRA-TEST-KEY:PO-10057
    Examples:
      | conflicting_field       |
      | address_line_1          |
    @JIRA-TEST-KEY:PO-10058
    Examples:
      | conflicting_field       |
      | postcode                |
    @JIRA-TEST-KEY:PO-10059
    Examples:
      | conflicting_field       |
      | organisation_name       |
    @JIRA-TEST-KEY:PO-10060
    Examples:
      | conflicting_field       |
      | surname                 |
    @JIRA-TEST-KEY:PO-10061
    Examples:
      | conflicting_field       |
      | forenames_and_surname   |

  @JIRA-TEST-KEY:PO-10062
  Scenario: AC2 - Account number without creditor search fields is accepted
    When I search minor creditor accounts with only the created account number populated
    Then the minor creditor account search returns the created account

  @JIRA-TEST-KEY:PO-10063
  Scenario: AC3 - First name without last name returns bad request
    When I search minor creditor accounts with first name and no last name populated
    Then the minor creditor account search request is rejected as a schema bad request

  @JIRA-TEST-KEY:PO-10064
  Scenario: AC4 - First name with last name is accepted
    When I search minor creditor accounts with the created first name and last name populated
    Then the minor creditor account search returns the created account
