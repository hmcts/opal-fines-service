@Opal @JIRA-LABEL:account-enquiry @R1BDrop1
Feature: Defendant account party replacement

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @ExtendedTest @JIRA-STORY:PO-10767 @JIRA-EPIC:PO-10729
  Scenario: Updating an individual defendant emits an alteration log
    Given an auditable defendant account exists for submitted by "PDPOUPD001"
    When I replace the defendant party for the created defendant account
    Then the replace defendant account party response matches the documented contract
    And the logging service emits "Update Defendant" with category "Alteration" for the "Defendant" party

  @cleanUpData @ExtendedTest @JIRA-STORY:PO-10767 @JIRA-EPIC:PO-10729
  Scenario: Converting an individual to a company emits conversion and guardian removal logs
    Given an auditable defendant account exists for submitted by "PDPOCON001"
    When I add a non-paying Parent/Guardian to the created defendant account
    Then the add-party response identifies a non-paying Parent/Guardian with a party ID
    When I convert the defendant party for the created defendant account to a company
    Then the converted defendant party response identifies a company
    And the logging service emits "Convert Individual to Company" with category "Erasure" for the "Defendant" party
    And the logging service emits "Remove Parent/Guardian" with category "Erasure" for the "Parent/Guardian" party

  # Only visible editable fields are populated. Non-paying guardians have names and address only.
  # Employer details apply to paying individuals; vehicle and contact details also apply to companies.
  # Phone fixtures use internal spaces: 35 raw characters and exactly 11 digits.
  @cleanUpData @PartyMaximumLength @ExtendedTest @JIRA-STORY:PO-11196 @JIRA-EPIC:PO-10729
  Scenario Outline: Maximum-length <source> to <target> party amendment or conversion
    Given a <source> party exists for maximum-length amendment
    When I replace the party as a <target> using the maximum-length editable fixture
      | group             | path                                                                            | length | value                                                                        |
      | individual        | /party_details/individual_details/forenames                                     | 20     | Amended forenames AZ                                                         |
      | full-individual   | /party_details/individual_details/individual_aliases/0/forenames                | 20     | Alias forenames AliZ                                                         |
      | individual        | /party_details/individual_details/surname                                       | 30     | Amended surname Amended surnaZ                                               |
      | full-individual   | /party_details/individual_details/individual_aliases/0/surname                  | 30     | Alias surname Alias surname AZ                                               |
      | full-individual   | /party_details/individual_details/national_insurance_number                     | 9      | AB123456C                                                                    |
      | company           | /party_details/organisation_details/organisation_name                           | 50     | Amended Company Amended Company Amended Company AZ                           |
      | company           | /party_details/organisation_details/organisation_aliases/0/organisation_name    | 20     | Trading Alias TradiZ                                                         |
      | all               | /address/address_line_1                                                         | 30     | Party address 1 Party addressZ                                               |
      | all               | /address/address_line_2                                                         | 30     | Party address 2 Party addressZ                                               |
      | defendant-address | /address/address_line_3                                                         | 16     | Defendant town Z                                                             |
      | guardian-address  | /address/address_line_3                                                         | 13     | Guardian towZ                                                                |
      | all               | /address/postcode                                                               | 8      | AB12 3CD                                                                     |
      | contact           | /contact_details/primary_email_address                                          | 76     | pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp@example.com |
      | contact           | /contact_details/secondary_email_address                                        | 76     | ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss@example.com |
      | contact           | /contact_details/mobile_telephone_number                                        | 35     | 0   7   1   2   3  4  5  6  7  8  9                                          |
      | contact           | /contact_details/home_telephone_number                                          | 35     | 0   2   0   7   9  4  6  0  1  2  3                                          |
      | contact           | /contact_details/work_telephone_number                                          | 35     | 0   1   6   3   2  9  6  0  1  2  3                                          |
      | vehicle           | /vehicle_details/vehicle_make_and_model                                         | 30     | Vehicle make model Vehicle maZ                                               |
      | vehicle           | /vehicle_details/vehicle_registration                                           | 11     | AB12 CDEFGH                                                                  |
      | employer          | /employer_details/employer_name                                                 | 50     | Amended employer Amended employer Amended employeZ                           |
      | employer          | /employer_details/employer_reference                                            | 20     | Employee reference Z                                                         |
      | employer          | /employer_details/employer_email_address                                        | 76     | eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee@example.com |
      | employer          | /employer_details/employer_telephone_number                                     | 35     | 0   1   3   3   2  9  6  0  1  2  3                                          |
      | employer          | /employer_details/employer_address/address_line_1                               | 30     | Employer address 1 Employer aZ                                               |
      | employer          | /employer_details/employer_address/address_line_2                               | 30     | Employer address 2 Employer aZ                                               |
      | employer          | /employer_details/employer_address/address_line_3                               | 30     | Employer address 3 Employer aZ                                               |
      | employer          | /employer_details/employer_address/postcode                                     | 8      | ZX98 7YW                                                                     |
    Then the party replacement succeeds and GET preserves the maximum-length editable values

    Examples:
      | source              | target     |
      | individual          | individual |
      | company             | company    |
      | individual          | company    |
      | company             | individual |
      | paying-guardian     | individual |
      | non-paying-guardian | individual |
