@Opal @JIRA-LABEL:account-enquiry @R1BDrop1
Feature: Add a defendant account party

  Background:
    Given I am testing as the "opal-test@dev.platform.hmcts.net" user

  @cleanUpData @JIRA-STORY:PO-10745 @JIRA-EPIC:PO-10729
  Scenario: Add a non-paying Parent/Guardian to a defendant account
    Given an auditable defendant account exists for submitted by "PDPOPG001"
    When I add a non-paying Parent/Guardian to the created defendant account
    Then the add-party response identifies a non-paying Parent/Guardian with a party ID

  # Phone values contain internal spaces: 35 raw characters and 11 digits ignoring spaces.
  @cleanUpData @ExtendedTest @JIRA-STORY:PO-11195 @JIRA-EPIC:PO-10729
  @JIRA-DEFECT:PO-11950
  Scenario: Add a non-paying Parent/Guardian with visible text fields at their limits
    Given an auditable defendant account exists for submitted by "PDPO11195"
    When I add a non-paying Parent/Guardian using the maximum-length visible fixture
      | path                                                         | length | value                                                                        |
      | /party_details/individual_details/forenames                  | 20     | Guardian forenames Z                                                         |
      | /party_details/individual_details/surname                    | 30     | Guardian surname Guardian surZ                                               |
      | /address/address_line_1                                      | 30     | Party address 1 Party addressZ                                               |
      | /address/address_line_2                                      | 30     | Party address 2 Party addressZ                                               |
      | /address/address_line_3                                      | 13     | Guardian towZ                                                                |
      | /address/postcode                                            | 8      | AB12 3CD                                                                     |
      | /contact_details/primary_email_address                       | 76     | pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp@example.com |
      | /contact_details/secondary_email_address                     | 76     | ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss@example.com |
      | /contact_details/mobile_telephone_number                     | 35     | 0   7   1   2   3  4  5  6  7  8  9                                          |
      | /contact_details/home_telephone_number                       | 35     | 0   2   0   7   9  4  6  0  1  2  3                                          |
      | /contact_details/work_telephone_number                       | 35     | 0   1   6   3   2  9  6  0  1  2  3                                          |
    Then the added Parent/Guardian can be identified on the account
    When I retrieve the added Parent/Guardian party details
    Then the added Parent/Guardian details preserve the visible fixture content and lengths
