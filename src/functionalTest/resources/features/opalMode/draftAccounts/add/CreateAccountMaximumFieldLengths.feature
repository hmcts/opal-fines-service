@Opal @JIRA-LABEL:manual-account-creation @R1A @JIRA-STORY:11192 @JIRA-EPIC:PO-50 @Ignore
Feature: Create accounts with maximum-length fields

  @cleanUpData @JIRA-DEFECT:PO-11118
  Scenario: Published individual account retains maximum-length header values
    Given a defendant account with header summary data exists for submitted by "MAXLEN-IND-001" using fixture "draftAccounts/accountJson/individualMaxLengths.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains originator type "TFO"
    And the defendant account header summary contains originator name "Humber Magistrates' Court"
    And the defendant account header summary contains collection order "true"
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains party details forenames "FFFFFFFFFFFFFFFFFFFF" surname "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | PCR-11111111111111111111111111 |
    When I request the created defendant account "at-a-glance" response
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | party_details.individual_details.forenames | FFFFFFFFFFFFFFFFFFFF | 20 |
      | party_details.individual_details.surname | SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS | 30 |
      | party_details.individual_details.national_insurance_number | OT000002B | 9 |
      | party_details.individual_details.individual_aliases[0].forenames | AAAAAAAAAAAAAAAAAAAA | 20 |
      | party_details.individual_details.individual_aliases[0].surname | BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB | 30 |
      | address.address_line_1 | 123456789012345678901234567890 | 30 |
      | address.address_line_2 | ABCDEFGHIJABCDEFGHIJABCDEFGHIJ | 30 |
      | address.address_line_3 | ABCDEFGHIJKLMNOP | 16 |
      | address.postcode | SW1A 1AA | 8 |
      | comments_and_notes.account_comment | COMMENT-CCCCCCCCCCCCCCCCCCCCCC | 30 |
      | payment_terms.payment_terms_type.payment_terms_type_code | B | 1 |
      | payment_terms.effective_date | 2027-01-01 | 10 |
      | enforcement_status.default_days_in_jail | 99999 | 5 |
    When I request the created defendant account party details
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | defendant_account_party.party_details.individual_details.forenames | FFFFFFFFFFFFFFFFFFFF | 20 |
      | defendant_account_party.party_details.individual_details.surname | SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS | 30 |
      | defendant_account_party.party_details.individual_details.national_insurance_number | OT000002B | 9 |
      | defendant_account_party.party_details.individual_details.individual_aliases[0].forenames | AAAAAAAAAAAAAAAAAAAA | 20 |
      | defendant_account_party.party_details.individual_details.individual_aliases[0].surname | BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB | 30 |
      | defendant_account_party.address.address_line_1 | 123456789012345678901234567890 | 30 |
      | defendant_account_party.address.address_line_2 | ABCDEFGHIJABCDEFGHIJABCDEFGHIJ | 30 |
      | defendant_account_party.address.address_line_3 | ABCDEFGHIJKLMNOP | 16 |
      | defendant_account_party.address.postcode | SW1A 1AA | 8 |
      | defendant_account_party.contact_details.mobile_telephone_number | 07123456789 | 11 |
      | defendant_account_party.contact_details.primary_email_address | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com | 76 |
      | defendant_account_party.contact_details.secondary_email_address | bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb@example.com | 76 |
      | defendant_account_party.vehicle_details.vehicle_make_and_model | MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM | 30 |
      | defendant_account_party.vehicle_details.vehicle_registration | AB12 CDEFGH | 11 |
      | defendant_account_party.employer_details.employer_name | EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE | 35 |
      | defendant_account_party.employer_details.employer_reference | RRRRRRRRRRRRRRRRRRRR | 20 |
      | defendant_account_party.employer_details.employer_email_address | cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc@example.com | 76 |
      | defendant_account_party.employer_details.employer_address.address_line_1 | AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA | 30 |
      | defendant_account_party.employer_details.employer_address.address_line_2 | BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB | 30 |
      | defendant_account_party.employer_details.employer_address.address_line_3 | CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC | 30 |
      | defendant_account_party.employer_details.employer_address.address_line_4 | DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD | 30 |
      | defendant_account_party.employer_details.employer_address.address_line_5 | EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE | 30 |
      | defendant_account_party.employer_details.employer_address.postcode | SW1A 1AA | 8 |
    When I request the created defendant account "impositions" response
    Then the created defendant account response contains these exact numeric values
      | json_path | expected |
      | impositions[0].imposed_amount | -500 |
    When I request the created defendant account "payment-terms/latest" response
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | payment_terms.payment_terms_type.payment_terms_type_code | B | 1 |
      | payment_terms.effective_date | 2027-01-01 | 10 |

  @cleanUpData
  Scenario: Published company account retains maximum-length company details
    Given a defendant account with header summary data exists for submitted by "MAXLEN-COMP-001" using fixture "draftAccounts/accountJson/companyMaxLengths.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains party details organisation flag "true"
    And the defendant account header summary contains party details organisation name "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | PCR-11111111111111111111111111 |
    When I request the created defendant account "at-a-glance" response
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | party_details.organisation_details.organisation_name | CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC | 50 |

  @cleanUpData
  Scenario: Published parent or guardian account retains maximum-length party names
    Given a defendant account with header summary data exists for submitted by "MAXLEN-PG-001" using fixture "draftAccounts/accountJson/parentGuardianMaxLengths.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains party details forenames "FFFFFFFFFFFFFFFFFFFF" surname "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | PCR-11111111111111111111111111 |
    When I request the created defendant account "at-a-glance" response
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | party_details.individual_details.forenames | PPPPPPPPPPPPPPPPPPPPPPPPP | 25 |
      | party_details.individual_details.surname | GGGGGGGGGGGGGGGGGGGGGGGGG | 25 |

  @cleanUpData
  Scenario: Published fixed-penalty account retains maximum-length ticket details
    Given an enforceable defendant account exists with the following details
      | business_unit_id  | 77                                               |
      | account           | draftAccounts/accountJson/fixedPenaltyMaxLengths.json |
      | account_type      | Fixed Penalty                                    |
      | account_status    | Submitted                                        |
      | submitted_by      | MAXLEN-FP-001                                    |
      | submitted_by_name | Laura Clerk                                      |
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains the expected live values
      | fixed_penalty_ticket_number | NNNNNNNNNNNNNNNN |
    When I request the created defendant account "fixed-penalty" response
    Then the created defendant account response contains these exact values and lengths
      | json_path | expected | length |
      | fixed_penalty_ticket_details.ticket_number | NNNNNNNNNNNNNNNN | 16 |
      | fixed_penalty_ticket_details.place_of_offence | PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP | 30 |

  @cleanUpData
  Scenario: Published account retains maximum-length minor-creditor payment details
    Given a defendant account with header summary data exists for submitted by "MAXLEN-MC-001" using fixture "draftAccounts/accountJson/minorCreditorMaxLengths.json"
    When I request defendant account header summary for the created defendant account
    Then the defendant account header summary response is returned as documented
    And the defendant account header summary contains party details organisation flag "false"
    And the defendant account header summary contains the expected live values
      | prosecutor_case_reference | PCR-11111111111111111111111111 |