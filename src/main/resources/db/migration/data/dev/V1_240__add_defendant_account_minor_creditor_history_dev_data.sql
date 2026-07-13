/**
* OPAL Program
*
* MODULE      : add_defendant_account_minor_creditor_history_dev_data.sql
*
* DESCRIPTION : Adds a complete dev defendant account data set with linked minor creditor history.
*
* VERSION HISTORY:
*
* Date          Author    Version     Nature of Change
* ----------    ------    --------    ----------------------------------------------------------------------------
* 07/07/2026    P Brumby  1.0         PO-8302 - Add a complete dev defendant account data set with linked minor creditor history.
*
**/

INSERT INTO public.parties (
  party_id, organisation, organisation_name, surname, forenames, title,
  address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, account_type, birth_date, age, national_insurance_number,
  telephone_home, telephone_business, telephone_mobile, email_1, email_2,
  last_changed_date
)
VALUES
  (99000000000019, false, NULL, 'Roberts', 'Helen Marie', 'Ms',
   'Flat 19 Test House', 'Test Road', NULL, NULL, NULL,
   'ZZ1 19ZZ', NULL, '1991-01-01 00:00:00', NULL, NULL,
   NULL, NULL, NULL, NULL, NULL,
   NULL),
  (99000000000919, true, 'Minor Creditor History Office', NULL, NULL, NULL,
   '19 Administration Block', 'Birmingham', NULL, NULL, NULL,
   'B1 1BC', 'Creditor', NULL, NULL, NULL,
   NULL, NULL, NULL, NULL, NULL,
   NULL);

INSERT INTO public.debtor_detail (
  party_id, vehicle_make, vehicle_registration, employer_name,
  employer_address_line_1, employer_address_line_2, employer_address_line_3,
  employer_address_line_4, employer_address_line_5, employer_postcode,
  employee_reference, employer_telephone, employer_email,
  document_language, document_language_date, hearing_language, hearing_language_date
)
VALUES (
  99000000000019, 'Nissan', 'REG0019', 'History Services Corp',
  '1 Business Park', 'Unit 19', NULL,
  NULL, NULL, 'ZZ9 9ZZ',
  'EMP019019', NULL, NULL,
  NULL, NULL, NULL, NULL
);

INSERT INTO public.aliases (
  alias_id, party_id, surname, forenames, sequence_number, organisation_name
)
VALUES
  (99000000001130, 99000000000019, 'Robertson', 'Helen', 1, NULL),
  (99000000001131, 99000000000019, 'Roberts', 'Helen M', 2, NULL);

INSERT INTO public.creditor_accounts (
  creditor_account_id, business_unit_id, account_number, creditor_account_type,
  prosecution_service, major_creditor_id, minor_creditor_party_id,
  from_suspense, hold_payout, pay_by_bacs,
  bank_sort_code, bank_account_number, bank_account_name, bank_account_reference,
  bank_account_type, last_changed_date, version_number
)
VALUES (
  99000000000819, 77, '87654340', 'MN',
  false, NULL, 99000000000919,
  false, false, true,
  '191919', '19191919', 'Minor Creditor His', 'REF019',
  NULL, NULL, 1
);

INSERT INTO public.defendant_accounts (
  defendant_account_id, business_unit_id, account_number, imposed_hearing_date,
  imposing_court_id, amount_imposed, amount_paid, account_balance,
  account_status, completed_date, enforcing_court_id, last_hearing_court_id,
  last_hearing_date, last_movement_date, last_changed_date, last_enforcement,
  originator_name, originator_type, allow_writeoffs, allow_cheques,
  cheque_clearance_period, credit_trans_clearance_period, enf_override_result_id,
  enf_override_enforcer_id, enf_override_tfo_lja_id, unit_fine_detail,
  unit_fine_value, collection_order, collection_order_date,
  further_steps_notice_date, confiscation_order_date, fine_registration_date,
  suspended_committal_date, consolidated_account_type, payment_card_requested,
  payment_card_requested_date, payment_card_requested_by, prosecutor_case_reference,
  enforcement_case_status, originator_id, account_type, account_comments,
  account_note_1, account_note_2, account_note_3, jail_days, version_number,
  payment_card_requested_by_name, imposed_by_name
)
VALUES (
  99000000000019, 77, '77777777J', '2026-05-19 11:37:55.196682',
  50000000001, 1250.00, 275.00, 975.00,
  'L', NULL, 50000000001, 50000000001,
  NULL, '2026-05-21 11:37:55.196682', '2026-05-21 11:37:55.196682', 'ENF1',
  'Fixed Penalty Office', 'FP', false, true,
  5, 3, NULL,
  NULL, NULL, NULL,
  NULL, false, NULL,
  NULL, NULL, '2026-05-19 11:37:55.196682',
  NULL, NULL, true,
  '2026-05-21 11:37:55.196682', 'L019HM', 'FP2024000019',
  NULL, 'FP19000019', 'Fixed Penalty', 'Fixed penalty account with minor creditor history',
  'Minor creditor financial activity seeded', 'Creditor account notes seeded',
  'Creditor account amendments seeded', NULL, 1,
  'Test User 19', NULL
);

INSERT INTO public.defendant_account_parties (
  defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
)
VALUES (
  99000000002019, 99000000000019, 99000000000019, 'Defendant', true
);

INSERT INTO public.fixed_penalty_offences (
  defendant_account_id, ticket_number, vehicle_registration, offence_location,
  notice_number, issued_date, licence_number, vehicle_fixed_penalty,
  offence_date, offence_time
)
VALUES (
  99000000000019, 'FP24000019', 'REG0019', 'History Test Car Park',
  NULL, '2026-05-19', NULL, NULL,
  '2026-05-19', '13:45'
);

INSERT INTO public.payment_card_requests (defendant_account_id)
VALUES (99000000000019);

INSERT INTO public.document_instances (
  document_instance_id, document_id, business_unit_id, generated_date, generated_by,
  associated_record_type, associated_record_id, status, printed_date, document_content
)
VALUES (
  99000000012019, 'ABD', 77, '2026-05-21 11:37:55.196682', 'L019HM',
  'defendant_accounts', '99000000000019', 'New', NULL,
  '<doc><account>99000000000019</account><type>minor-creditor-history</type></doc>'
);

INSERT INTO public.report_entries (
  report_entry_id, business_unit_id, report_id, entry_timestamp, reported_timestamp,
  associated_record_type, associated_record_id, report_instance_id
)
VALUES (
  99000000009019, 77, 'fp_register', '2026-05-21 11:37:55.196682', '2026-05-21 11:37:55.196682',
  'defendant_accounts', '99000000000019', 99000000008000
);

INSERT INTO public.enforcements (
  enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason,
  enforcer_id, jail_days, result_responses, warrant_reference, case_reference,
  hearing_date, hearing_court_id, posted_by_name, earliest_release_date,
  enforcement_account_type
)
VALUES (
  99000000007019, 99000000000019, '2026-05-19 11:37:55.196682', 'L019HM', NULL,
  'Minor creditor history test enforcement', NULL, NULL, '{"type":"minor-creditor-history"}',
  '007/25/00019', NULL, NULL, NULL, 'Test User 19', NULL, NULL
);

INSERT INTO public.payment_terms (
  payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code,
  effective_date, instalment_period, instalment_amount, instalment_lump_sum,
  jail_days, extension, account_balance, posted_by_name, active, reason_for_extension
)
VALUES (
  99000000013019, 99000000000019, '2026-05-19 11:37:55.196682', 'L019HM', 'I',
  '2026-05-21 00:00:00', 'M', 125.00, 0.00,
  NULL, false, 975.00, 'Test User 19', true, NULL
);

INSERT INTO public.impositions (
  imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name,
  original_posted_date, result_id, imposing_court_id, imposed_date,
  imposed_amount, paid_amount, offence_id, offence_title, offence_code,
  creditor_account_id, unit_fine_adjusted, unit_fine_units, completed,
  original_imposition_id
)
VALUES
  (99000000003019, 99000000000019, '2026-05-19 11:37:55.196682', 'L019HM', 'Test User 19',
   NULL, 'ABDC', NULL, NULL,
   600.00, 125.00, NULL, 'Minor creditor history offence 1', 'MCH001',
   99000000000819, NULL, NULL, NULL,
   NULL),
  (99000000003020, 99000000000019, '2026-05-20 11:37:55.196682', 'L019HM', 'Test User 19',
   NULL, 'ABDC', NULL, NULL,
   650.00, 150.00, NULL, 'Minor creditor history offence 2', 'MCH002',
   99000000000819, NULL, NULL, NULL,
   NULL);

INSERT INTO public.defendant_transactions (
  defendant_transaction_id, defendant_account_id, posted_date, posted_by,
  transaction_type, transaction_amount, payment_method, payment_reference,
  text, status, status_date, status_amount, write_off_code,
  associated_record_type, associated_record_id, imposed_amount, posted_by_name
)
VALUES
  (99000000004019, 99000000000019, '2026-05-19 12:37:55.196682', 'L019HM',
   'PAYMNT', 125.00, 'CT', 'PMT000019',
   'Card payment for minor creditor history account', 'C', '2026-05-19 12:37:55.196682', NULL, NULL,
   NULL, NULL, NULL, 'Test User 19'),
  (99000000004020, 99000000000019, '2026-05-20 12:37:55.196682', 'L019HM',
   'PAYMNT', 150.00, 'CT', 'PMT000020',
   'Second card payment for minor creditor history acc', 'C', '2026-05-20 12:37:55.196682', NULL, NULL,
   NULL, NULL, NULL, 'Test User 19');

INSERT INTO public.allocations (
  allocation_id, imposition_id, allocated_date, allocated_amount,
  transaction_type, allocation_function, defendant_transaction_id
)
VALUES
  (99000000005019, 99000000003019, '2026-05-19 12:37:55.196682', 125.00,
   'PAYMNT', 'AutoTest', 99000000004019),
  (99000000005020, 99000000003020, '2026-05-20 12:37:55.196682', 150.00,
   'PAYMNT', 'AutoTest', 99000000004020);

INSERT INTO public.creditor_transactions (
  creditor_transaction_id, creditor_account_id, posted_date, posted_by, posted_by_name,
  transaction_type, transaction_amount, imposition_result_id, payment_processed,
  payment_reference, status, status_date, associated_record_type, associated_record_id
)
VALUES
  (99000000006019, 99000000000819, '2026-05-19 12:37:55.196682', 'L019HM', 'Test User 19',
   'PAYMNT', 125.00, 'ABDC', true,
   'MCH000019', 'C', '2026-05-19 12:37:55.196682', 'defendant_accounts', '99000000000019'),
  (99000000006020, 99000000000819, '2026-05-20 12:37:55.196682', 'L019HM', 'Test User 19',
   'PAYMNT', 150.00, 'ABDC', true,
   'MCH000020', 'C', '2026-05-20 12:37:55.196682', 'defendant_accounts', '99000000000019');

INSERT INTO public.notes (
  note_id, note_type, associated_record_type, associated_record_id,
  note_text, posted_date, posted_by, posted_by_name
)
VALUES
  (99000000011019, 'AA', 'creditor_accounts', '99000000000819',
   'Minor creditor history note one for defendant account 99000000000019',
   '2026-05-21 09:00:00', 'L019HM', 'Test User 19'),
  (99000000011020, 'AA', 'creditor_accounts', '99000000000819',
   'Minor creditor history note two for defendant account 99000000000019',
   '2026-05-22 09:00:00', 'L019HM', 'Test User 19');

INSERT INTO public.amendments (
  amendment_id, business_unit_id, associated_record_type, associated_record_id,
  amended_date, amended_by, amended_by_name, field_code, old_value, new_value,
  case_reference, function_code
)
VALUES
  (99000000014019, 77, 'creditor_accounts', '99000000000819',
   '2026-05-23 10:00:00', 'L019HM', 'Test User 19', 41, 'false', 'true',
   NULL, 'minor-creditor-history'),
  (99000000014020, 77, 'creditor_accounts', '99000000000819',
   '2026-05-24 10:00:00', 'L019HM', 'Test User 19', 42, 'false', 'true',
   NULL, 'minor-creditor-history');
