/**
* CGI OPAL Program
*
* MODULE      : draft_account_deletion_tactical_report_TEST_DATA.sql
*
* DESCRIPTION : Insert test data into DRAFT_ACCOUNTS to test the draft_account_deleteion_tactical_report.sql PSQL script.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -------------------------------------------------------------------
* 16/02/2026    T McCallion    1.0         PO-2756 - Seed Data for Draft Account Deletion Tactical Report
*
**/

--TRUNCATE draft_accounts;

DO $$
DECLARE
    v_next_da_id INT;

    v_prev_mm_first         DATE;
    v_prev_mm_first_ts      TIMESTAMP;
    v_prev_mm_last          DATE;
    v_prev_mm_first_less_1  DATE;
    v_prev_mm_last_plus_1   DATE;
    v_leap_year_date        DATE := '2024-02-29';
BEGIN

    SELECT COALESCE(MAX(draft_account_id), 0) + 1 INTO v_next_da_id FROM draft_accounts;
    
    SELECT date_trunc('month', CURRENT_DATE - INTERVAL '1 month')::date INTO v_prev_mm_first;
    SELECT (date_trunc('month', CURRENT_DATE) - INTERVAL '1 day')::date INTO v_prev_mm_last;
    v_prev_mm_first_less_1 := v_prev_mm_first - INTERVAL '1 day';
    v_prev_mm_last_plus_1 := v_prev_mm_last + INTERVAL '1 day';
    v_prev_mm_first_ts := to_timestamp(to_char(v_prev_mm_first, 'YYYY-MM-DD') || ' 13:15', 'YYYY-MM-DD HH24:MI:SS');

    RAISE NOTICE 'v_prev_mm_first = %', v_prev_mm_first;
    RAISE NOTICE 'v_prev_mm_last = %', v_prev_mm_last;
    RAISE NOTICE 'v_prev_mm_first_less_1 = %', v_prev_mm_first_less_1;
    RAISE NOTICE 'v_prev_mm_last_plus_1 = %', v_prev_mm_last_plus_1;
    RAISE NOTICE 'v_leap_year_date = %', v_leap_year_date; 

    RAISE NOTICE 'First draft_account_id inserted = %', v_next_da_id;


    -- Insert 17 (8 DELETED) draft_accounts test entries with real JSONs, all statuses and multiple offences and impositions entries
    INSERT INTO draft_accounts (
        draft_account_id,
        business_unit_id,
        created_date,
        submitted_by,
        account,
        account_type,
        account_snapshot,
        account_status,
        timeline_data,
        submitted_by_name,
        account_status_date,
        version_number
    ) VALUES
        -- 1 SUBMITTED  -- Should be Excluded from the report output
        (
            v_next_da_id,
            77,
            v_prev_mm_first,
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2008-09-14", "title": "Mr", "gender": null, "pnc_id": null, "surname": "MORGAN", "forenames": "Liam James", "post_code": "NW1 6XE", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "aliases": [{"alias_surname": "SMITH", "alias_forenames": "Jay Thomas", "alias_company_name": null}], "vehicle_make": "Peugeot 208", "vehicle_registration_mark": "YX08 WDE", "hearing_language": null, "document_language": null, "employee_reference": "QQ654321D", "employer_post_code": "AL1 5XZ", "employer_company_name": "Greenfield Garden Services", "employer_email_address": "admin@greenfieldgardens.co.uk", "employer_address_line_1": "Unit 12", "employer_address_line_2": "Park Farm Estate", "employer_address_line_3": "Lemsford Lane", "employer_address_line_4": "Hatfield", "employer_address_line_5": "Herts", "employer_telephone_number": "01727889900" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "Flat 5B", "address_line_2": "221B Baker Street", "address_line_3": "Marylebone", "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "liam.morgan@example.com", "email_address_2": "liam.j.morgan@gmail.com", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 7123 9876", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 900456", "national_insurance_number": "QQ654321D", "telephone_number_business": "0161 496 2001" }, "account_type": "Fine", "account_notes": [ { "note_type": "AC", "account_note_text": "Under 18 – flag for youth handling", "account_note_serial": 3 }, { "note_type": "AA", "account_note_text": "Youth defendant. Still in part-time education. Collection order issued 22/02/2023. Parent/guardian details pending. Confirm contact via email before enforcement.", "account_note_serial": 2 }, { "note_type": "AA", "account_note_text": "A collection order was previously made on 22/02/2023 prior to this account creation", "account_note_serial": 1 } ], "originator_id": 402, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Bradford Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-02-15", "collection_order_date": "2023-02-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "21MET409283", "collection_order_made_today": null }'::JSON,
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || 'T11:15:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2008-09-14", "defendant_name": "MORGAN, Liam James", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'SUBMITTED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first,
            0
        ),
        -- 2 SUBMITTED  -- Should be Excluded from the report output
        (
            v_next_da_id + 1,
            77,
            v_prev_mm_first,
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FCPC", "amount_paid": 0, "amount_imposed": 80, "minor_creditor": null, "major_creditor_id": null}, {"result_id": "FO", "amount_paid": 0, "amount_imposed": 45, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "15/02/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2010-05-18", "title": "Miss", "gender": null, "pnc_id": null, "surname": "BENNETT", "forenames": "Ava Grace", "post_code": "BS1 4ST", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "Flat 2A", "address_line_2": "12 Kings Square", "address_line_3": "Stokes Croft", "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "ava.bennett@studentmail.com", "email_address_2": "ava.g.bennett@gmail.com", "parent_guardian": { "company_flag": false, "company_name": null, "surname": "BENNETT", "forenames": "Sophia", "dob": "1982-03-09", "national_insurance_number": "QQ112233C", "address_line_1": "24 St Marks Road", "address_line_2": "Easton", "address_line_3": "Bristol", "address_line_4": "Avon", "address_line_5": null, "post_code": "BS5 6JD", "telephone_number_home": "0117 654 3210", "telephone_number_business": "0117 998 2301", "telephone_number_mobile": "07700 934567", "email_address_1": "sophia.bennett@greenmail.com", "email_address_2": "s.bennett78@gmail.com", "debtor_detail": { "vehicle_make": "Nissan Micra", "vehicle_registration_mark": "BX12 HKA", "document_language": null, "hearing_language": null, "employee_reference": "QQ112233C", "employer_company_name": "South West Care Co.", "employer_address_line_1": "The Orchard Building", "employer_address_line_2": "25 Elder Street", "employer_address_line_3": "Stokes Croft", "employer_address_line_4": "Bristol", "employer_address_line_5": null, "employer_post_code": "BS1 3TP", "employer_telephone_number": "0117 456 9900", "employer_email_address": "hr@southwestcare.org", "aliases": [{"alias_surname": "WRIGHT", "alias_forenames": "Sophie Marie", "alias_company_name": null}] },"title":null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0117 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 912345", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AC", "account_note_text": "Parent or guardian responsible for payment", "account_note_serial": 3 }, { "note_type": "AA", "account_note_text": "Child defendant. Debtor is parent Sophia Bennett. Contact all communications through guardian. Collection order created 22/02/2023.", "account_note_serial": 2 }, { "note_type": "AA", "account_note_text": "A collection order was previously made on 22/02/2023 prior to this account creation", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "pgToPay", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-02-15", "collection_order_date": "2023-02-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023456789", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || 'T11:45:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2010-05-18", "defendant_name": "BENNETT, Ava Grace", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'SUBMITTED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first,
            0
        ),
        -- 3 REJECTED  -- Should be Excluded from the report output
        (
            v_next_da_id + 2,
            77,
            v_prev_mm_first + INTERVAL '1 day',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "10/05/2023", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2003-05-10", "title": "Mr", "gender": null, "pnc_id": null, "surname": "SMITH", "forenames": "John", "post_code": "W1A 1AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "12 Main Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "john.smith@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 7000 1111", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 111222", "national_insurance_number": "QQ123456A", "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Account rejected due to missing post code.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type": "NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2023-05-10", "collection_order_date": "2023-05-15", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123456", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || 'T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2003-05-10", "defendant_name": "SMITH, John", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'REJECTED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing post code.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '1 day',
            0
        ),
        -- 4 REJECTED (already resubmitted once)  -- Should be Excluded from the report output
        (
            v_next_da_id + 3,
            77,
            v_prev_mm_first + INTERVAL '1 day',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 50, "amount_imposed": 125, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "30/12/1975", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1975-12-30", "title": "Ms", "gender": "F", "pnc_id": null, "surname": "TAYLOR", "forenames": "Emily", "post_code": "M1 1AE", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": { "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" }, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "45 Market Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "emily.taylor@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0161 123 4567", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 333444", "national_insurance_number": "QQ223344B", "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Account rejected due to invalid NINO.", "account_note_serial": 1 }, { "note_type": "AA", "account_note_text": "Account notes missing.", "account_note_serial": 2 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1975-12-30", "collection_order_date": "1976-01-10", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023987654", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || 'T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1975-12-30", "defendant_name": "TAYLOR, Emily", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'REJECTED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Invalid NINO.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Account notes missing.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '1 day', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '1 day',
            0
        ),
        -- 5 DELETED (normal)  -- 1 offence and 1 imposition - Balance = 69.50
        (
            v_next_da_id + 4,
            77,
            v_prev_mm_first_ts,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":60.50, "amount_imposed":130, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"21/08/1992", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1992-08-21", "title":"Mr", "gender":"M", "pnc_id":null, "surname":"JONES", "forenames":"Michael", "post_code":"B2 4QA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":null, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"22 Station Road", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"michael.jones@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"0121 111 2222", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 222333", "national_insurance_number":"QQ334455D", "telephone_number_business":null }, "account_type": "Fine", "account_notes":[{ "note_type":"AA", "account_note_text":"Court code invalid.", "account_note_serial":1 }], "originator_id": 401, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail":null, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1992-08-21", "collection_order_date":"1992-09-01", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023987655", "collection_order_made_today":null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || 'T13:15:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1992-08-21", "defendant_name": "JONES, Michael", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Court code invalid.", "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 1, reason with a comma", "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first_ts,
            0
        ),
        -- 6 DELETED (company)  -- 1 offence and 2 impositions - Balance = 280
        (
            v_next_da_id + 5,
            77,
            v_prev_mm_first,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":40, "amount_imposed":180, "minor_creditor":null, "major_creditor_id":null },{ "result_id":"FVS", "amount_paid":50, "amount_imposed":190, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":null, "imposing_court_id":770000000021 }], "defendant":{ "company_flag":true, "company_name":"Acme Holdings Ltd", "dob":null, "title":null, "gender":null, "forenames":null, "surname":null, "pnc_id":null, "cro_number":null, "national_insurance_number":null, "occupation":null, "post_code":"OX1 1AA", "address_line_1":"1 High Street", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"contact@acmeholdings.co.uk", "email_address_2":null, "telephone_number_home":null, "telephone_number_mobile":null, "telephone_number_business":"01865 123456", "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "ethnicity_self_defined":null, "driving_licence_number":null, "debtor_detail":{ "document_language":null, "hearing_language":null, "aliases":[{ "alias_company_name":"Acme Ltd" }], "employee_reference":"EMPL123456", "employer_company_name":"Example Employer Ltd", "employer_address_line_1":"1 Employer Street", "employer_address_line_2":"Suite 100", "employer_address_line_3":"Business Park", "employer_address_line_4":"City", "employer_address_line_5":"Region", "employer_post_code":"EX1 2MP", "employer_telephone_number":"01234 567890", "employer_email_address":"hr@exampleemployer.com" } }, "account_type": "Fine", "account_notes":[{ "note_type":"AA", "account_note_text":"User deleted draft.", "account_note_serial":1 }], "originator_id": 401, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"company", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail":null, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":null, "collection_order_date":null, "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023123989", "collection_order_made_today":null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || 'T14:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Acme Holdings Ltd", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 2.", "status_date": "' || to_char(v_prev_mm_first, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first,
            0
        ),
        -- 7 DELETED (parent/guardian)  -- 2 offences and 1,2 impositions - Balance = 140
        (
            v_next_da_id + 6,
            77,
            v_prev_mm_first + INTERVAL '5 days',
            'L077JG',
            '{ "offences":[{ "offence_id":33364, "impositions":[{ "result_id":"FVS", "amount_paid":10, "amount_imposed":50, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"11/06/2013", "imposing_court_id":770000000021 },{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":20, "amount_imposed":60, "minor_creditor":null, "major_creditor_id":null },{ "result_id":"FVS", "amount_paid":40, "amount_imposed":100, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"12/06/2013", "imposing_court_id":770000000021 }], "defendant":{ "dob":"2013-06-11", "title":"Mr", "gender":"M", "pnc_id":null, "surname":"LEE", "forenames":"Oscar", "post_code":"CF10 1AA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":null, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"3 Castle Street", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"oscar.lee@example.com", "email_address_2":null, "parent_guardian":{ "company_flag":false, "company_name":null, "surname":"LEE", "forenames":"Susan", "dob":"1985-02-02", "national_insurance_number":"QQ556677D", "address_line_1":"3 Castle Street", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "post_code":"CF10 1AA", "telephone_number_home":"029 1234 5678", "telephone_number_business":null, "telephone_number_mobile":"07700 999888", "email_address_1":"susan.lee@example.com", "email_address_2":null, "debtor_detail":{ "employee_reference":"EMPL123456", "employer_company_name":"Example Employer Ltd", "employer_address_line_1":"1 Employer Street", "employer_address_line_2":"Suite 100", "employer_address_line_3":"Business Park", "employer_address_line_4":"City", "employer_address_line_5":"Region", "employer_post_code":"EX1 2MP", "employer_telephone_number":"01234 567890", "employer_email_address":"hr@exampleemployer.com" }, "title":null }, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"029 8765 4321", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 777666", "national_insurance_number":null, "telephone_number_business":null }, "account_type": "Fixed Penalty", "account_notes":[{ "note_type":"AA", "account_note_text":"Missing employer details.", "account_note_serial":1 }], "originator_id": 402, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"pgToPay", "originator_name": "Bradford Crown Court", "originator_type":"NEW", "fp_ticket_detail":{ "notice_number":"FP54321" }, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"2013-06-11", "collection_order_date":"2013-06-20", "collection_order_made":true, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023123990", "collection_order_made_today":null }',
            'Fixed Penalty',
            ('{"account_type": "Fixed Penalty", "created_date": "2025-05-24T15:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2013-06-11", "defendant_name": "LEE, Oscar", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '5 days', 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing employer details.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '5 days', 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '5 days', 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 3.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '5 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '5 days',
            0
        ),
        -- 8 DELETED (no resubmission)  -- 2 offences and 2,2 impositions - Balance = 235
        (
            v_next_da_id + 7,
            77,
            v_prev_mm_first + INTERVAL '10 days',
            'L077JG',
            '{ "offences":[{ "offence_id":33364, "impositions":[{ "result_id":"FVS", "amount_paid":20, "amount_imposed":90, "minor_creditor":null, "major_creditor_id":null },{ "result_id":"FVS", "amount_paid":30, "amount_imposed":95, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"01/01/2000", "imposing_court_id":770000000021 },{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":20, "amount_imposed":60, "minor_creditor":null, "major_creditor_id":null },{ "result_id":"FVS", "amount_paid":40, "amount_imposed":100, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"01/01/2000", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1980-01-01", "title":"Mrs", "gender":"F", "pnc_id":null, "surname":"WALKER", "forenames":"Anna", "post_code":"G1 2FF", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":null, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"7 Riverbank Road", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"anna.walker@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"0141 123 4567", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 444555", "national_insurance_number":"QQ667788E", "telephone_number_business":null }, "account_type": "Fine", "account_notes":[{ "note_type":"AA", "account_note_text":"User deleted draft.", "account_note_serial":1 }], "originator_id": 401, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail":null, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1980-01-01", "collection_order_date":"1980-01-10", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023123991", "collection_order_made_today":null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || 'T16:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1980-01-01", "defendant_name": "WALKER, Anna", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 4.", "status_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '10 days',
            0
        ),
        -- 9 PUBLISHING_PENDING  -- Should be Excluded from the report output
        (
            v_next_da_id + 8,
            77,
            v_prev_mm_first + INTERVAL '10 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 70, "amount_imposed": 120, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "10/10/2000", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2000-10-10", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "CLARK", "forenames": "Sam", "post_code": "E1 7AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "4 Tower Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "sam.clark@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "020 8000 4444", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 555666", "national_insurance_number": "QQ778899F", "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for account.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2000-10-10", "collection_order_date": "2000-10-20", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123992", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || 'T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2000-10-10", "defendant_name": "CLARK, Sam", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'PUBLISHING_PENDING',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || '"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '10 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '10 days',
            0
        ),
        -- 10 PUBLISHING_PENDING (company)  -- Should be Excluded from the report output
        (
            v_next_da_id + 9,
            77,
            v_prev_mm_first + INTERVAL '11 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 110, "amount_imposed": 210, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Blue Sky Enterprises", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "BR1 1AA", "address_line_1": "100 Blue Lane", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "info@blueskyent.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "020 8000 7777", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Blue Sky Ltd"}], "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" } }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing pending for company account.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123993", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || 'T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Blue Sky Enterprises", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'PUBLISHING_PENDING',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '11 days',
            0
        ),
        -- 11 PUBLISHING_FAILED  -- Should be Excluded from the report output
        (
            v_next_da_id + 10,
            77,
            v_prev_mm_first + INTERVAL '11 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 80, "amount_imposed": 150, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "09/09/1999", "imposing_court_id": 770000000021 }], "defendant": { "dob": "1999-09-09", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "GREEN", "forenames": "Oliver", "post_code": "NG1 6AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "9 Forest Road", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "oliver.green@example.com", "email_address_2": null, "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0115 123 9876", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 666555", "national_insurance_number": "QQ990011H", "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Integration error.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "1999-09-09", "collection_order_date": "1999-09-20", "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123995", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || 'T09:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1999-09-09", "defendant_name": "GREEN, Oliver", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'PUBLISHING_FAILED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '11 days',
            0
        ),
        -- 12 PUBLISHING_FAILED (company)  -- Should be Excluded from the report output
        (
            v_next_da_id + 11,
            77,
            v_prev_mm_first + INTERVAL '11 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 75, "amount_imposed": 175, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": null, "imposing_court_id": 770000000021 }], "defendant": { "company_flag": true, "company_name": "Sunrise Logistics", "dob": null, "title": null, "gender": null, "forenames": null, "surname": null, "pnc_id": null, "cro_number": null, "national_insurance_number": null, "occupation": null, "post_code": "BA1 2AA", "address_line_1": "2 Transport Way", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "admin@sunriselogistics.co.uk", "email_address_2": null, "telephone_number_home": null, "telephone_number_mobile": null, "telephone_number_business": "01225 123456", "parent_guardian": null, "interpreter_lang": null, "ethnicity_observed": null, "ethnicity_self_defined": null, "driving_licence_number": null, "debtor_detail": { "document_language": null, "hearing_language": null, "aliases": [{"alias_company_name": "Sunrise Logistics UK"}], "employee_reference": "EMPL123456", "employer_company_name": "Example Employer Ltd", "employer_address_line_1": "1 Employer Street", "employer_address_line_2": "Suite 100", "employer_address_line_3": "Business Park", "employer_address_line_4": "City", "employer_address_line_5": "Region", "employer_post_code": "EX1 2MP", "employer_telephone_number": "01234 567890", "employer_email_address": "hr@exampleemployer.com" } }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Timeout.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "company", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": null, "collection_order_date": null, "collection_order_made": false, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123996", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || 'T10:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": null, "defendant_name": "Sunrise Logistics", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'PUBLISHING_FAILED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '11 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '11 days',
            0
        ),
        -- 13 PUBLISHING_FAILED (parent/guardian)  -- Should be Excluded from the report output
        (
            v_next_da_id + 12,
            77,
            v_prev_mm_first + INTERVAL '12 days',
            'L077JG',
            '{"offences": [{"offence_id": 33369, "impositions": [ {"result_id": "FVS", "amount_paid": 65, "amount_imposed": 165, "minor_creditor": null, "major_creditor_id": null} ], "date_of_sentence": "12/12/2009", "imposing_court_id": 770000000021 }], "defendant": { "dob": "2009-12-12", "title": "Mr", "gender": "M", "pnc_id": null, "surname": "KING", "forenames": "Jacob", "post_code": "S1 2AA", "cro_number": null, "occupation": null, "company_flag": false, "company_name": null, "debtor_detail": null, "nationality_1": null, "nationality_2": null, "prison_number": null, "address_line_1": "66 Queen Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "custody_status": null, "email_address_1": "jacob.king@example.com", "email_address_2": null, "parent_guardian": { "company_flag": false, "company_name": null, "surname": "KING", "forenames": "Martin", "dob": "1981-08-08", "national_insurance_number": "QQ112244A", "address_line_1": "66 Queen Street", "address_line_2": null, "address_line_3": null, "address_line_4": null, "address_line_5": null, "post_code": "S1 2AA", "telephone_number_home": "0114 234 5678", "telephone_number_business": null, "telephone_number_mobile": "07700 222333", "email_address_1": "martin.king@example.com", "email_address_2": null, "debtor_detail": null }, "interpreter_lang": null, "ethnicity_observed": null, "telephone_number_home": "0114 876 5432", "driving_licence_number": null, "ethnicity_self_defined": null, "telephone_number_mobile": "07700 333444", "national_insurance_number": null, "telephone_number_business": null }, "account_type": "Fine", "account_notes": [ { "note_type": "AA", "account_note_text": "Publishing failed: Court not found.", "account_note_serial": 1 } ], "originator_id": 401, "payment_terms": { "enforcements": null, "effective_date": "2023-03-22", "lump_sum_amount": 50, "instalment_amount": 25, "instalment_period": "W", "default_days_in_jail": null, "payment_terms_type_code": "I" }, "defendant_type": "pgToPay", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail": null, "enforcement_court_id": 770000000021, "payment_card_request": null, "account_sentence_date": "2009-12-12", "collection_order_date": "2009-12-22", "collection_order_made": true, "suspended_committal_date": null, "prosecutor_case_reference": "CRN2023123997", "collection_order_made_today": null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first + INTERVAL '12 days', 'YYYY-MM-DD') || 'T11:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "2009-12-12", "defendant_name": "KING, Jacob", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'PUBLISHING_FAILED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '12 days', 'YYYY-MM-DD') || '"},{"status": "Approved", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first + INTERVAL '12 days', 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first + INTERVAL '12 days',
            0
        ),
        -- 14 DELETED (with rejected and resubmitted)  -- 1 offence and 1 imposition - Balance = 55.
        (
            v_next_da_id + 13,
            77,
            v_prev_mm_last,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":45, "amount_imposed":100, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"03/03/1997", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1997-03-03", "title":"Ms", "gender":"F", "pnc_id":null, "surname":"EVANS", "forenames":"Lucy", "post_code":"PL1 1AA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":{ "employee_reference":"EMPL123456", "employer_company_name":"Example Employer Ltd", "employer_address_line_1":"1 Employer Street", "employer_address_line_2":"Suite 100", "employer_address_line_3":"Business Park", "employer_address_line_4":"City", "employer_address_line_5":"Region", "employer_post_code":"EX1 2MP", "employer_telephone_number":"01234 567890", "employer_email_address":"hr@exampleemployer.com" }, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"101 Harbour View", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"lucy.evans@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"01752 123456", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 555444", "national_insurance_number":"QQ445577A", "telephone_number_business":null }, "account_type":"Fixed Penalty", "account_notes":[{ "note_type":"AA", "account_note_text":"Missing DOB.", "account_note_serial":1 },{ "note_type":"AA", "account_note_text":"User deleted draft.", "account_note_serial":2 }], "originator_id":2091, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name":"Northampton, Daventry and", "originator_type":"FP", "fp_ticket_detail":{ "notice_number":"FP12345" }, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1997-03-03", "collection_order_date":"1997-03-10", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023123999", "collection_order_made_today":null }',
            'Fixed Penalty',
            ('{"account_type": "Fixed Penalty", "created_date": "' || to_char(v_prev_mm_last, 'YYYY-MM-DD') || 'T12:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1997-03-03", "defendant_name": "EVANS, Lucy", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_last, 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing DOB.", "status_date": "' || to_char(v_prev_mm_last, 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_last, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 5.", "status_date": "' || to_char(v_prev_mm_last, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_last,
            0
        ),
        -- 15 DELETED (with rejected and resubmitted)  -- 1 offence and 1 imposition - Balance = 54.
        (
            v_next_da_id + 14,
            77,
            v_prev_mm_last_plus_1,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":46, "amount_imposed":100, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"03/03/1997", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1997-04-03", "title":"Mr", "gender":"M", "pnc_id":null, "surname":"SMITH", "forenames":"Tim", "post_code":"PL1 1AA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":{ "employee_reference":"EMPL123456", "employer_company_name":"Example Employer Ltd", "employer_address_line_1":"1 Employer Street", "employer_address_line_2":"Suite 100", "employer_address_line_3":"Business Park", "employer_address_line_4":"City", "employer_address_line_5":"Region", "employer_post_code":"EX1 2MP", "employer_telephone_number":"01234 567890", "employer_email_address":"hr@exampleemployer.com" }, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"101 Harbour View", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"tim.smith@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"01752 123456", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 555444", "national_insurance_number":"QQ445577B", "telephone_number_business":null }, "account_type":"Fixed Penalty", "account_notes":[{ "note_type":"AA", "account_note_text":"Missing DOB.", "account_note_serial":1 },{ "note_type":"AA", "account_note_text":"User deleted draft.", "account_note_serial":2 }], "originator_id":2091, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name":"Northampton, Daventry and", "originator_type":"FP", "fp_ticket_detail":{ "notice_number":"FP56789" }, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1997-03-03", "collection_order_date":"1997-03-10", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023123999", "collection_order_made_today":null }',
            'Fixed Penalty',
            ('{"account_type": "Fixed Penalty", "created_date": "' || to_char(v_prev_mm_last_plus_1, 'YYYY-MM-DD') || 'T12:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1997-04-03", "defendant_name": "SMITH, Tim", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_last_plus_1, 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Missing DOB.", "status_date": "' || to_char(v_prev_mm_last_plus_1, 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_last_plus_1, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 6.", "status_date": "' || to_char(v_prev_mm_last_plus_1, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_last_plus_1,
            0
        ),
        -- 16 DELETED (normal)  -- 1 offence and 1 imposition - Balance = 79.50
        (
            v_next_da_id + 15,
            77,
            v_prev_mm_first_less_1,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":50.50, "amount_imposed":130, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"21/08/1992", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1992-08-21", "title":"Mr", "gender":"M", "pnc_id":null, "surname":"JONES", "forenames":"James", "post_code":"B2 4QA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":null, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"22 Station Road", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"michael.jones@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"0121 111 2222", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 222333", "national_insurance_number":"QQ334455G", "telephone_number_business":null }, "account_type": "Fine", "account_notes":[{ "note_type":"AA", "account_note_text":"Court code invalid.", "account_note_serial":1 }], "originator_id": 401, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail":null, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1992-08-21", "collection_order_date":"1992-09-01", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023987655", "collection_order_made_today":null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_prev_mm_first_less_1, 'YYYY-MM-DD') || 'T13:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1992-08-21", "defendant_name": "JONES, James", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first_less_1, 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Court code invalid.", "status_date": "' || to_char(v_prev_mm_first_less_1, 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_prev_mm_first_less_1, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 7, reason with a comma", "status_date": "' || to_char(v_prev_mm_first_less_1, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_prev_mm_first_less_1,
            0
        ),
        -- 17 DELETED (normal) - leap year  -- 1 offence and 1 imposition - Balance = 69.50
        (
            v_next_da_id + 16,
            77,
            v_leap_year_date,
            'L077JG',
            '{ "offences":[{ "offence_id":33369, "impositions":[{ "result_id":"FVS", "amount_paid":50.50, "amount_imposed":120, "minor_creditor":null, "major_creditor_id":null }], "date_of_sentence":"21/08/1992", "imposing_court_id":770000000021 }], "defendant":{ "dob":"1992-08-21", "title":"Mr", "gender":"M", "pnc_id":null, "surname":"JONES", "forenames":"James", "post_code":"B2 4QA", "cro_number":null, "occupation":null, "company_flag":false, "company_name":null, "debtor_detail":null, "nationality_1":null, "nationality_2":null, "prison_number":null, "address_line_1":"22 Station Road", "address_line_2":null, "address_line_3":null, "address_line_4":null, "address_line_5":null, "custody_status":null, "email_address_1":"michael.jones@example.com", "email_address_2":null, "parent_guardian":null, "interpreter_lang":null, "ethnicity_observed":null, "telephone_number_home":"0121 111 2222", "driving_licence_number":null, "ethnicity_self_defined":null, "telephone_number_mobile":"07700 222333", "national_insurance_number":"QQ334455G", "telephone_number_business":null }, "account_type": "Fine", "account_notes":[{ "note_type":"AA", "account_note_text":"Court code invalid.", "account_note_serial":1 }], "originator_id": 401, "payment_terms":{ "enforcements":null, "effective_date":"2023-03-22", "lump_sum_amount":50, "instalment_amount":25, "instalment_period":"W", "default_days_in_jail":null, "payment_terms_type_code":"I" }, "defendant_type":"adultOrYouthOnly", "originator_name": "Aylesbury Crown Court", "originator_type":"NEW", "fp_ticket_detail":null, "enforcement_court_id":770000000021, "payment_card_request":null, "account_sentence_date":"1992-08-21", "collection_order_date":"1992-09-01", "collection_order_made":false, "suspended_committal_date":null, "prosecutor_case_reference":"CRN2023987655", "collection_order_made_today":null }',
            'Fine',
            ('{"account_type": "Fine", "created_date": "' || to_char(v_leap_year_date, 'YYYY-MM-DD') || 'T13:00:00.000000Z", "submitted_by": "L077JG", "date_of_birth": "1992-08-21", "defendant_name": "JONES, James", "submitted_by_name": "opal-test", "business_unit_name": "Camberwell Green"}')::JSON,
            'DELETED',
            ('[{"status": "Submitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_leap_year_date, 'YYYY-MM-DD') || '"},{"status": "Rejected", "username": "opal-test", "reason_text": "Court code invalid.", "status_date": "' || to_char(v_leap_year_date, 'YYYY-MM-DD') || '"},{"status": "Resubmitted", "username": "opal-test", "reason_text": null, "status_date": "' || to_char(v_leap_year_date, 'YYYY-MM-DD') || '"},{"status": "Deleted", "username": "opal-test", "reason_text": "User deleted draft 8, reason with a comma", "status_date": "' || to_char(v_leap_year_date, 'YYYY-MM-DD') || '"}]')::JSON,
            'opal-test',
            v_leap_year_date,
            0
        )
    ;

END $$;
