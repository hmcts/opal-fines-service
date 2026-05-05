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
    name,
    warrant_reference_sequence,
    warrant_register_sequence
)
VALUES (
           780000000021, 78, 21, 'North East Enforcement', NULL, NULL
       )
ON CONFLICT (enforcer_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        enforcer_code = EXCLUDED.enforcer_code,
        name = EXCLUDED.name,
        warrant_reference_sequence = EXCLUDED.warrant_reference_sequence,
        warrant_register_sequence = EXCLUDED.warrant_register_sequence;

INSERT INTO local_justice_areas (
    local_justice_area_id,
    lja_code,
    name,
    address_line_1,
    address_line_4,
    address_line_5,
    end_date
)
VALUES (
           230, 'L240', 'Tyne & Wear LJA', 'Test LJA Address Line 1', NULL, NULL, NULL
       )
ON CONFLICT (local_justice_area_id) DO UPDATE
    SET lja_code = EXCLUDED.lja_code,
        name = EXCLUDED.name,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_4 = EXCLUDED.address_line_4,
        address_line_5 = EXCLUDED.address_line_5,
        end_date = EXCLUDED.end_date;

INSERT INTO courts (
    court_id, business_unit_id, court_code, parent_court_id, name, name_cy,
    address_line_1, address_line_2, address_line_3,
    address_line_1_cy, address_line_2_cy, address_line_3_cy, postcode,
    local_justice_area_id, national_court_code, gob_enforcing_court_code,
    lja, court_type, division, session,
    start_time, max_load, record_session_times, max_court_duration, group_code
)
VALUES (
           1, 78, 7, 730000000103, 'AAA Test Court', NULL,
           'TestVille', 'TestShire', NULL,
           NULL, NULL, NULL, NULL,
           230, NULL, NULL,
           230, 'MC', '01', NULL,
           NULL, NULL, NULL, NULL, NULL
       )
ON CONFLICT (court_id) DO UPDATE
    SET business_unit_id = EXCLUDED.business_unit_id,
        court_code = EXCLUDED.court_code,
        parent_court_id = EXCLUDED.parent_court_id,
        name = EXCLUDED.name,
        name_cy = EXCLUDED.name_cy,
        address_line_1 = EXCLUDED.address_line_1,
        address_line_2 = EXCLUDED.address_line_2,
        address_line_3 = EXCLUDED.address_line_3,
        address_line_1_cy = EXCLUDED.address_line_1_cy,
        address_line_2_cy = EXCLUDED.address_line_2_cy,
        address_line_3_cy = EXCLUDED.address_line_3_cy,
        postcode = EXCLUDED.postcode,
        local_justice_area_id = EXCLUDED.local_justice_area_id,
        national_court_code = EXCLUDED.national_court_code,
        gob_enforcing_court_code = EXCLUDED.gob_enforcing_court_code,
        lja = EXCLUDED.lja,
        court_type = EXCLUDED.court_type,
        division = EXCLUDED.division,
        session = EXCLUDED.session,
        start_time = EXCLUDED.start_time,
        max_load = EXCLUDED.max_load,
        record_session_times = EXCLUDED.record_session_times,
        max_court_duration = EXCLUDED.max_court_duration,
        group_code = EXCLUDED.group_code;

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
    address_line_4,
    address_line_5,
    postcode,
    account_type,
    birth_date,
    age,
    national_insurance_number,
    telephone_home,
    telephone_business,
    telephone_mobile,
    email_1,
    email_2,
    last_changed_date
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
           NULL,
           NULL,
           'MA4 1AL',
           'Defendant',
           '1980-02-03 00:00:00',
           33,
           'A11111A',
           '12345',
           '67890',
           '111111',
           'email@one.com',
           'email@two.com',
           NULL
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
        address_line_4 = EXCLUDED.address_line_4,
        address_line_5 = EXCLUDED.address_line_5,
        postcode = EXCLUDED.postcode,
        account_type = EXCLUDED.account_type,
        birth_date = EXCLUDED.birth_date,
        age = EXCLUDED.age,
        national_insurance_number = EXCLUDED.national_insurance_number,
        telephone_home = EXCLUDED.telephone_home,
        telephone_business = EXCLUDED.telephone_business,
        telephone_mobile = EXCLUDED.telephone_mobile,
        email_1 = EXCLUDED.email_1,
        email_2 = EXCLUDED.email_2,
        last_changed_date = EXCLUDED.last_changed_date;

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
    document_language_date,
    hearing_language,
    hearing_language_date
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
           NULL,
           'EN',
           NULL
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
        document_language_date = EXCLUDED.document_language_date,
        hearing_language = EXCLUDED.hearing_language,
        hearing_language_date = EXCLUDED.hearing_language_date;

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
    completed_date,
    enforcing_court_id,
    last_hearing_court_id,
    last_hearing_date,
    last_movement_date,
    last_changed_date,
    last_enforcement,
    originator_name,
    originator_id,
    originator_type,
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
    confiscation_order_date,
    fine_registration_date,
    suspended_committal_date,
    consolidated_account_type,
    payment_card_requested,
    payment_card_requested_date,
    payment_card_requested_by,
    prosecutor_case_reference,
    enforcement_case_status,
    account_type,
    account_comments,
    account_note_1,
    account_note_2,
    account_note_3,
    jail_days
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
           500.58,
           'L',
           NULL,
           1,
           1,
           '2024-01-04 18:06:11',
           '2024-01-02 17:08:09',
           '2024-01-03 12:00:12',
           '10',
           'Kingston-upon-Thames Mags Court',
           NULL,
           NULL,
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
           NULL,
           NULL,
           NULL,
           'M',
           'Y',
           '2024-01-01 00:00:00',
           '11111111A',
           '090A',
           NULL,
           'Fine',
           'Text - Account Comment',
           'free_text_note_1',
           'free_text_note_2',
           'free_text_note_3',
           101
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
        completed_date = EXCLUDED.completed_date,
        enforcing_court_id = EXCLUDED.enforcing_court_id,
        last_hearing_court_id = EXCLUDED.last_hearing_court_id,
        last_hearing_date = EXCLUDED.last_hearing_date,
        last_movement_date = EXCLUDED.last_movement_date,
        last_changed_date = EXCLUDED.last_changed_date,
        last_enforcement = EXCLUDED.last_enforcement,
        originator_name = EXCLUDED.originator_name,
        originator_id = EXCLUDED.originator_id,
        originator_type = EXCLUDED.originator_type,
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
        confiscation_order_date = EXCLUDED.confiscation_order_date,
        fine_registration_date = EXCLUDED.fine_registration_date,
        suspended_committal_date = EXCLUDED.suspended_committal_date,
        consolidated_account_type = EXCLUDED.consolidated_account_type,
        payment_card_requested = EXCLUDED.payment_card_requested,
        payment_card_requested_date = EXCLUDED.payment_card_requested_date,
        payment_card_requested_by = EXCLUDED.payment_card_requested_by,
        prosecutor_case_reference = EXCLUDED.prosecutor_case_reference,
        enforcement_case_status = EXCLUDED.enforcement_case_status,
        account_type = EXCLUDED.account_type,
        account_comments = EXCLUDED.account_comments,
        account_note_1 = EXCLUDED.account_note_1,
        account_note_2 = EXCLUDED.account_note_2,
        account_note_3 = EXCLUDED.account_note_3,
        jail_days = EXCLUDED.jail_days;

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


INSERT INTO enforcements (
    enforcement_id,
    defendant_account_id,
    posted_date,
    posted_by,
    result_id,
    reason,
    enforcer_id,
    jail_days,
    result_responses,
    warrant_reference,
    case_reference,
    hearing_date,
    hearing_court_id,
    posted_by_name,
    earliest_release_date,
    enforcement_account_type
)
VALUES (
           1,
           77,
           TIMESTAMP '2000-01-01',
           'L080JG',
           'REGF',
           'Test enforcement',
           NULL,
           NULL,
           NULL,
           '001/25/00001',
           NULL,
           NULL,
           1,
           'opal-test',
           NULL,
           NULL
       )
ON CONFLICT (enforcement_id) DO UPDATE
    SET defendant_account_id = EXCLUDED.defendant_account_id,
        posted_date = EXCLUDED.posted_date,
        posted_by = EXCLUDED.posted_by,
        result_id = EXCLUDED.result_id,
        reason = EXCLUDED.reason,
        enforcer_id = EXCLUDED.enforcer_id,
        jail_days = EXCLUDED.jail_days,
        result_responses = EXCLUDED.result_responses,
        warrant_reference = EXCLUDED.warrant_reference,
        case_reference = EXCLUDED.case_reference,
        hearing_date = EXCLUDED.hearing_date,
        hearing_court_id = EXCLUDED.hearing_court_id,
        posted_by_name = EXCLUDED.posted_by_name,
        earliest_release_date = EXCLUDED.earliest_release_date,
        enforcement_account_type = EXCLUDED.enforcement_account_type;