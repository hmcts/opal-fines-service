@Opal @R1BDrop2 @JIRA-LABEL:account-enquiry @ExcludeFromDemoNightlyAsNotLegacy
Feature: Minor creditor account amendments and conversions

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @MinorCreditorUpdatePdpo @JIRA-STORY:PO-10765 @JIRA-EPIC:PO-10729 @ExtendedTest
  Scenario: Updating an individual minor creditor emits an alteration log
    Given an individual minor creditor account exists for amendment
    When I update the created minor creditor account details
    Then the minor creditor update returns the changed details and preserves version behaviour
    And the logging service emits "Update Minor Creditor" with category "Alteration" for the created minor creditor account

  @MinorCreditorUpdatePdpo @JIRA-STORY:PO-10765 @JIRA-EPIC:PO-10729 @ExtendedTest
  Scenario: Updating a company minor creditor does not emit an individual alteration log
    Given a company minor creditor account exists for amendment
    When I update the created minor creditor account details
    Then the minor creditor update returns the changed details and preserves version behaviour
    And the logging service does not emit "Update Minor Creditor" for the created minor creditor account

  @MinorCreditorMaximumLength @R1BDrop2 @JIRA-STORY:11192 @JIRA-EPIC:PO-50
  Scenario Outline: Maximum-length <source> to <target> amendment with BACS account number <account_number>
    Given a <source> minor creditor account exists for amendment
    When I amend the minor creditor to a <target> with BACS account number "<account_number>" and maximum-length details
      | group      | field             | value                                              | length |
      | individual | forenames         | Alexandria Catherine                               | 20     |
      | individual | surname           | Montgomery Wellington Hamilton                     | 30     |
      | company    | organisation_name | Maximum Length Minor Creditor Organisation Limited | 50     |
      | address    | address_line_1    | 123 Maximum Length Test Street                     | 30     |
      | address    | address_line_2    | Maximum Length Business Centre                     | 30     |
      | address    | address_line_3    | Test District AB                                   | 16     |
      | address    | address_line_4    | Test County ABCD                                   | 16     |
      | address    | address_line_5    | Test Country ABC                                   | 16     |
      | address    | postcode          | SW1A 1AA                                           | 8      |
      | BACS       | account_name      | Max Account Holder                                 | 18     |
      | BACS       | account_reference | MAX-REFERENCE-1234                                 | 18     |
      | BACS       | sort_code         | 012345                                             | 6      |
    Then the minor creditor amendment succeeds and GET returns the submitted maximum-length details

    Examples:
      | source     | target     | account_number |
      | individual | individual | none           |
      | individual | individual | 012345         |
      | individual | individual | 0123456        |
      | individual | individual | 01234567       |
      | company    | company    | none           |
      | company    | company    | 012345         |
      | company    | company    | 0123456        |
      | company    | company    | 01234567       |
      | individual | company    | none           |
      | individual | company    | 012345         |
      | individual | company    | 0123456        |
      | individual | company    | 01234567       |
      | company    | individual | none           |
      | company    | individual | 012345         |
      | company    | individual | 0123456        |
      | company    | individual | 01234567       |
