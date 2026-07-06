/**
* OPAL Program
*
* MODULE      : insert_into_enforcements.sql
*
* DESCRIPTION : Insert test data for enforcements integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     -------     ---------------------------------------------------------------------------------------------------------
* 01/05/2026    S WALL       1.0         PO2255 Insert test data for enforcements integration tests
* 26/05/2026    S WALL       1.1         PO2255 Add impositions data for enforcement integration tests
* 16/06/2026    S WALL       1.2         PO2256 Amend enforcements data for integration tests
**/

SET search_path TO public;

INSERT INTO business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type,
    welsh_language
)
VALUES (
           78, 'N E Region', 'NE', 'Area', FALSE
       )
ON CONFLICT (business_unit_id) DO UPDATE
    SET business_unit_name = EXCLUDED.business_unit_name,
        business_unit_code = EXCLUDED.business_unit_code,
        business_unit_type = EXCLUDED.business_unit_type,
        welsh_language = EXCLUDED.welsh_language;

INSERT INTO enforcers (
    enforcer_id,
    business_unit_id,
    enforcer_code,
    name
)
VALUES (
           780000000021, 78, 21, 'North East Enforcement'
       )
ON CONFLICT (enforcer_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        enforcer_code = EXCLUDED.enforcer_code,
        name = EXCLUDED.name;

INSERT INTO local_justice_areas (
    local_justice_area_id,
    lja_code,
    name,
    address_line_1
)
VALUES (
           230, 'L240', 'Tyne & Wear LJA', 'Test LJA Address Line 1'
       )
ON CONFLICT (local_justice_area_id) DO UPDATE
    SET lja_code = EXCLUDED.lja_code,
        name = EXCLUDED.name,
        address_line_1 = EXCLUDED.address_line_1;

INSERT INTO courts (
    court_id, business_unit_id, court_code, parent_court_id, name,
    address_line_1, address_line_2,
    local_justice_area_id,
    lja, court_type, division
)
VALUES (
           1, 78, 7, 730000000103, 'AAA Test Court',
           'TestVille', 'TestShire',
           230,
           230, 'MC', '01'
       )
ON CONFLICT (court_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        court_code = EXCLUDED.court_code,
        parent_court_id = EXCLUDED.parent_court_id,
        name = EXCLUDED.name,
        name_cy = EXCLUDED.name_cy,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_2 = EXCLUDED.address_line_2,
        local_justice_area_id = EXCLUDED.local_justice_area_id,
        lja = EXCLUDED.lja,
        court_type = EXCLUDED.court_type,
        division = EXCLUDED.division;

INSERT INTO parties (
    party_id,
    organisation,
    organisation_name,
    surname,
    forenames,
    title,
    address_line_1,
    address_line_2,
    address_line_3,
    postcode,
    account_type,
    birth_date,
    age,
    national_insurance_number,
    telephone_home,
    telephone_business,
    telephone_mobile,
    email_1,
    email_2
)
VALUES (
           77,
           'N',
           'Sainsco',
           'Graham',
           'Anna',
           'Ms',
           'Lumber House',
           '77 Gordon Road',
           'Maidstone, Kent',
           'MA4 1AL',
           'Defendant',
           '1980-02-03 00:00:00',
           33,
           'A11111A',
           '12345',
           '67890',
           '111111',
           'email@one.com',
           'email@two.com'
       )
ON CONFLICT (party_id) DO UPDATE
    SET organisation = EXCLUDED.organisation,
        organisation_name = EXCLUDED.organisation_name,
        surname = EXCLUDED.surname,
        forenames = EXCLUDED.forenames,
        title = EXCLUDED.title,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_2 = EXCLUDED.address_line_2,
        address_line_3 = EXCLUDED.address_line_3,
        postcode = EXCLUDED.postcode,
        account_type = EXCLUDED.account_type,
        birth_date = EXCLUDED.birth_date,
        age = EXCLUDED.age,
        national_insurance_number = EXCLUDED.national_insurance_number,
        telephone_home = EXCLUDED.telephone_home,
        telephone_business = EXCLUDED.telephone_business,
        telephone_mobile = EXCLUDED.telephone_mobile,
        email_1 = EXCLUDED.email_1,
        email_2 = EXCLUDED.email_2;

INSERT INTO debtor_detail (
    party_id,
    vehicle_make,
    vehicle_registration,
    employer_name,
    employer_address_line_1,
    employer_address_line_2,
    employer_address_line_3,
    employer_address_line_4,
    employer_address_line_5,
    employer_postcode,
    employee_reference,
    employer_telephone,
    employer_email,
    document_language,
    hearing_language
)
VALUES (
           77,
           'Toyota Prius',
           'AB77CDE',
           'Tesco Ltd',
           '123 Employer Road',
           'Employer Lane',
           'London Borough',
           'London',
           'England',
           'EMP1 2AA',
           'EMPREF77',
           '02079997777',
           'employer77@company.com',
           'EN',
           'EN'
       )
ON CONFLICT (party_id) DO UPDATE
    SET vehicle_make = EXCLUDED.vehicle_make,
        vehicle_registration = EXCLUDED.vehicle_registration,
        employer_name = EXCLUDED.employer_name,
        employer_address_line_1 = EXCLUDED.employer_address_line_1,
        employer_address_line_2 = EXCLUDED.employer_address_line_2,
        employer_address_line_3 = EXCLUDED.employer_address_line_3,
        employer_address_line_4 = EXCLUDED.employer_address_line_4,
        employer_address_line_5 = EXCLUDED.employer_address_line_5,
        employer_postcode = EXCLUDED.employer_postcode,
        employee_reference = EXCLUDED.employee_reference,
        employer_telephone = EXCLUDED.employer_telephone,
        employer_email = EXCLUDED.employer_email,
        document_language = EXCLUDED.document_language,
        hearing_language = EXCLUDED.hearing_language;

INSERT INTO defendant_accounts (
    defendant_account_id,
    version_number,
    business_unit_id,
    account_number,
    imposed_hearing_date,
    imposing_court_id,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    enforcing_court_id,
    last_hearing_court_id,
    last_hearing_date,
    last_movement_date,
    last_changed_date,
    last_enforcement,
    originator_name,
    allow_writeoffs,
    allow_cheques,
    cheque_clearance_period,
    credit_trans_clearance_period,
    enf_override_result_id,
    enf_override_enforcer_id,
    enf_override_tfo_lja_id,
    unit_fine_detail,
    unit_fine_value,
    collection_order,
    collection_order_date,
    further_steps_notice_date,
    consolidated_account_type,
    payment_card_requested,
    payment_card_requested_date,
    payment_card_requested_by,
    prosecutor_case_reference,
    account_type,
    account_comments,
    account_note_1,
    account_note_2,
    account_note_3,
    jail_days,
    originator_type
)
VALUES (
           77,
           0,
           78,
           '177A',
           '2023-11-03 16:05:10',
           1,
           700.58,
           200.00,
           -500.58,
           'L',
           1,
           1,
           '2024-01-04 18:06:11',
           '2024-01-02 17:08:09',
           '2024-01-03 12:00:12',
           'ABDC',
           'Kingston-upon-Thames Mags Court',
           'N',
           'N',
           14,
           21,
           'FWEC',
           780000000021,
           230,
           'GB pound sterling',
           700.00,
           'Y',
           '2023-12-18 00:00:00',
           '2023-12-19 00:00:00',
           'M',
           'Y',
           '2024-01-01 00:00:00',
           '11111111A',
           '090A',
           'Fine',
           'Text - Account Comment',
           'free_text_note_1',
           'free_text_note_2',
           'free_text_note_3',
           101,
           'NEW'
       ),
       (
           78,
           0,
           78,
           'ConsolidatedAcc',
           '2023-11-03',
           1,
           700.58,
           200.00,
           -500.58,
           'L',
           1,
           1,
           '2024-01-04',
           '2024-01-02',
           '2024-01-03',
           'ABDC',
           'Kingston-upon-Thames Mags Court',
           true,
           false,
           14,
           21,
           'FWEC',
           780000000021,
           230,
           'GB pound sterling',
           700.00,
           true,
           '2023-12-18',
           '2023-12-19',
           'M',
           true,
           '2024-01-01',
           '11111111A',
           '090A',
           'Fine',
           'Text - Account Comment',
           'free_text_note_1',
           'free_text_note_2',
           'free_text_note_3',
           101,
           'NEW'
       ),
       (
           79,
           0,
           78,
           'noPaymentsAfterEnf',
           '2023-11-03',
           1,
           700.58,
           200.00,
           -500.58,
           'L',
           1,
           1,
           '2024-01-04',
           '2024-01-02',
           '2024-01-03',
           'ABDC',
           'Kingston-upon-Thames Mags Court',
           true,
           false,
           14,
           21,
           'FWEC',
           780000000021,
           230,
           'GB pound sterling',
           700.00,
           true,
           '2023-12-18',
           '2023-12-19',
           'M',
           true,
           '2024-01-01',
           '11111111A',
           '090A',
           'Fine',
           'Text - Account Comment',
           'free_text_note_1',
           'free_text_note_2',
           'free_text_note_3',
           101,
           'NEW'
       )
ON CONFLICT (defendant_account_id) DO UPDATE
    SET version_number = EXCLUDED.version_number,
        business_unit_id = EXCLUDED.business_unit_id,
        account_number = EXCLUDED.account_number,
        imposed_hearing_date = EXCLUDED.imposed_hearing_date,
        imposing_court_id = EXCLUDED.imposing_court_id,
        amount_imposed = EXCLUDED.amount_imposed,
        amount_paid = EXCLUDED.amount_paid,
        account_balance = EXCLUDED.account_balance,
        account_status = EXCLUDED.account_status,
        enforcing_court_id = EXCLUDED.enforcing_court_id,
        last_hearing_court_id = EXCLUDED.last_hearing_court_id,
        last_hearing_date = EXCLUDED.last_hearing_date,
        last_movement_date = EXCLUDED.last_movement_date,
        last_changed_date = EXCLUDED.last_changed_date,
        last_enforcement = EXCLUDED.last_enforcement,
        originator_name = EXCLUDED.originator_name,
        allow_writeoffs = EXCLUDED.allow_writeoffs,
        allow_cheques = EXCLUDED.allow_cheques,
        cheque_clearance_period = EXCLUDED.cheque_clearance_period,
        credit_trans_clearance_period = EXCLUDED.credit_trans_clearance_period,
        enf_override_result_id = EXCLUDED.enf_override_result_id,
        enf_override_enforcer_id = EXCLUDED.enf_override_enforcer_id,
        enf_override_tfo_lja_id = EXCLUDED.enf_override_tfo_lja_id,
        unit_fine_detail = EXCLUDED.unit_fine_detail,
        unit_fine_value = EXCLUDED.unit_fine_value,
        collection_order = EXCLUDED.collection_order,
        collection_order_date = EXCLUDED.collection_order_date,
        further_steps_notice_date = EXCLUDED.further_steps_notice_date,
        consolidated_account_type = EXCLUDED.consolidated_account_type,
        payment_card_requested = EXCLUDED.payment_card_requested,
        payment_card_requested_date = EXCLUDED.payment_card_requested_date,
        payment_card_requested_by = EXCLUDED.payment_card_requested_by,
        prosecutor_case_reference = EXCLUDED.prosecutor_case_reference,
        account_type = EXCLUDED.account_type,
        account_comments = EXCLUDED.account_comments,
        account_note_1 = EXCLUDED.account_note_1,
        account_note_2 = EXCLUDED.account_note_2,
        account_note_3 = EXCLUDED.account_note_3,
        jail_days = EXCLUDED.jail_days,
        originator_type = EXCLUDED.originator_type;

INSERT INTO payment_terms
( payment_terms_id, defendant_account_id, posted_date, posted_by
, terms_type_code, effective_date, instalment_period, instalment_amount, instalment_lump_sum
, jail_days, extension, account_balance, active)
VALUES ( 0077, 0077, '2023-11-03 16:05:10', '01000000A'
       , 'B', '2025-10-12 00:00:00', 'W', NULL, NULL
       , 120, 'N', 700.58, true)
ON CONFLICT (payment_terms_id) DO UPDATE
      SET defendant_account_id = EXCLUDED.defendant_account_id,
      posted_date = EXCLUDED.posted_date,
      posted_by = EXCLUDED.posted_by,
      terms_type_code = EXCLUDED.terms_type_code,
      effective_date = EXCLUDED.effective_date,
      instalment_period = EXCLUDED.instalment_period,
      instalment_amount = EXCLUDED.instalment_amount,
      instalment_lump_sum = EXCLUDED.instalment_lump_sum,
      jail_days = EXCLUDED.jail_days,
      extension = EXCLUDED.extension,
      account_balance = EXCLUDED.account_balance,
      active = EXCLUDED.active;


INSERT INTO defendant_account_parties (
    defendant_account_party_id,
    defendant_account_id,
    party_id,
    association_type,
    debtor
)
VALUES (
           1,
           77,
           77,
           'Defendant',
           'Y'
       )
ON CONFLICT (defendant_account_party_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        party_id = EXCLUDED.party_id,
        association_type = EXCLUDED.association_type,
        debtor = EXCLUDED.debtor;

INSERT INTO defendant_transactions (
    defendant_transaction_id,
    defendant_account_id,
    posted_date,
    posted_by_name,
    transaction_type,
    transaction_amount,
    status_date,
    associated_record_type,
    associated_record_id,
    status
)
VALUES
    (
        100001,
        77,
        DATE '2026-05-14',
        'enforcement.test',
        'CONSOL',
        123.45,
        TIMESTAMP '2026-05-14 10:00:00',
        'defendant_accounts',
        '78',
        'P'
    ),
    (
        100002,
        77,
        DATE '2026-05-14',
        'enforcement.test',
        'PAYMNT',
        50.00,
        TIMESTAMP '2026-05-14 10:05:00',
        'defendant_accounts',
        NULL,
        'P'
    ),
    (
        10003,
        79,
        DATE '2000-05-14',
        'enforcement.test',
        'PAYMNT',
        50.00,
        TIMESTAMP '2026-05-14 10:05:00',
        'defendant_accounts',
        NULL,
        'C'
    )
    ON CONFLICT (defendant_transaction_id)
    DO UPDATE SET
    defendant_account_id   = EXCLUDED.defendant_account_id,
           posted_date            = EXCLUDED.posted_date,
           posted_by_name         = EXCLUDED.posted_by_name,
           transaction_type       = EXCLUDED.transaction_type,
           transaction_amount     = EXCLUDED.transaction_amount,
           status_date            = EXCLUDED.status_date,
           associated_record_type = EXCLUDED.associated_record_type,
           associated_record_id   = EXCLUDED.associated_record_id;



INSERT INTO enforcements (
    enforcement_id,
    defendant_account_id,
    posted_date,
    posted_by,
    result_id,
    reason,
    warrant_reference,
    hearing_court_id,
    posted_by_name
)
VALUES (
           1,
           77,
           TIMESTAMP '2000-01-01',
           'L080JG',
           'REGF',
           'Test enforcement',
           '001/25/00001',
           1,
           'opal-test'
       ),
       (
           2,
           77,
           TIMESTAMP '2000-01-02',
           'L080JG',
           'ABDC',
           'Test enforcement',
           '001/25/00001',
           1,
           'opal-test'
       ),
       (
           3,
           78,
           TIMESTAMP '2000-01-02',
           'L080JG',
           'ABDC',
           'Test enforcement',
           '001/25/00001',
           1,
           'opal-test'
       ),
       (
           4,
           79,
           TIMESTAMP '2010-01-02',
           'L080JG',
           'ABDC',
           'Test enforcement',
           '001/25/00001',
           1,
           'opal-test'
       ),
       (
           5,
           79,
           TIMESTAMP '2010-01-02',
           'L080JG',
           'REGF',
           'Test enforcement',
           '001/25/00001',
           1,
           'opal-test'
       )
ON CONFLICT (enforcement_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date,
        posted_by = EXCLUDED.posted_by,
        result_id = EXCLUDED.result_id,
        reason = EXCLUDED.reason,
        warrant_reference = EXCLUDED.warrant_reference,
        hearing_court_id = EXCLUDED.hearing_court_id,
        posted_by_name = EXCLUDED.posted_by_name;

INSERT INTO creditor_accounts(creditor_account_id, business_unit_id, account_number,
                              creditor_account_type, prosecution_service, from_suspense,
                              hold_payout, pay_by_bacs)
VALUES (1, 77, '177A', 'MJ',
        TRUE, FALSE, FALSE, FALSE);

INSERT INTO impositions(imposition_id, defendant_account_id, result_id, imposed_amount, posted_date,
                        paid_amount, creditor_account_id)
VALUES (1, 77, 'FCOMP', 50.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (2, 77, 'FCPC', 50.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (3, 77, 'FCOST', 50.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (4, 77, 'FO', 60.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (5, 77, 'FO', 60.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (6, 77, 'FVS', 70.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (7, 77, 'FVS', 80.00,
        '2023-11-03 16:05:10', 0.00, 1),
       (8, 77, 'FCC', 101.10,
        '2023-11-03 16:05:10', 0.00, 1),
       (9, 77, 'FCC', 200.70,
        '2023-11-03 16:05:10', 0.00, 1),
       (10, 77, 'DW', 101.10,
        '2023-11-03 16:05:10', 0.00, 1),
       (11, 77, 'CWN', 101.10,
        '2023-11-03 16:05:10', 0.00, 1),
       (12, 77, 'DW', 101.10,
        '2023-11-03 16:05:10', 0.00, 1),
       (13, 77, 'FCUEX', 101.10,
        '2023-11-03 16:05:10', 0.00, 1);