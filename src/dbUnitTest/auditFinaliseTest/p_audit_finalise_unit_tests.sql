\timing

/**
* CGI Opal Program
*
* MODULE      : p_audit_finalise_unit_tests.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_audit_finalise.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1677 Unit tests for p_audit_finalise.
*
**/

-- Clear out test data before tests
DO LANGUAGE 'plpgsql' $$
DECLARE

BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM amendments WHERE associated_record_id IN ('90001', '90003', '90004', '90005');
    DELETE FROM aliases WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3'));
    DELETE FROM debtor_detail WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3'));
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (SELECT defendant_account_id FROM defendant_accounts WHERE account_number LIKE 'TEST%');
    DELETE FROM creditor_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM defendant_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3');
    
    -- Delete test audit amendment fields (using field_code range for test data)
    DELETE FROM audit_amendment_fields WHERE field_code BETWEEN 9000 AND 9999;

    -- Drop any existing temp tables from previous sessions
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;

    RAISE NOTICE 'Data cleanup before tests completed';
END $$;

-- Insert test data for audit_amendment_fields
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Setting up audit_amendment_fields test data ===';
    
    -- Insert test audit amendment fields for defendant accounts
    INSERT INTO audit_amendment_fields (field_code, data_item) VALUES
        (9001, 'cheque_clearance_period'),
        (9002, 'allow_cheques'),
        (9003, 'credit_trans_clearance_period'),
        (9004, 'allow_writeoffs'),
        (9005, 'enf_override_enforcer_id'),
        (9006, 'enf_override_result_id'),
        (9007, 'enf_override_tfo_lja_id'),
        (9008, 'enforcing_court_id'),
        (9009, 'collection_order'),
        (9010, 'suspended_committal_date'),
        (9011, 'account_comments'),
        (9012, 'account_note_1'),
        (9013, 'account_note_2'),
        (9014, 'account_note_3'),
        (9015, 'name'),
        (9016, 'birth_date'),
        (9017, 'age'),
        (9018, 'address_line_1'),
        (9019, 'address_line_2'),
        (9020, 'address_line_3'),
        (9021, 'postcode'),
        (9022, 'national_insurance_number'),
        (9023, 'telephone_home'),
        (9024, 'telephone_business'),
        (9025, 'telephone_mobile'),
        (9026, 'email_1'),
        (9027, 'email_2'),
        (9028, 'pname'),
        (9029, 'paddr1'),
        (9030, 'paddr2'),
        (9031, 'paddr3'),
        (9032, 'pbdate'),
        (9033, 'pninumber'),
        (9034, 'alias1'),
        (9035, 'alias2'),
        (9036, 'alias3'),
        (9037, 'alias4'),
        (9038, 'alias5'),
        (9039, 'document_language'),
        (9040, 'hearing_language'),
        (9041, 'vehicle_make'),
        (9042, 'vehicle_registration'),
        (9043, 'employee_reference'),
        (9044, 'employer_name'),
        (9045, 'employer_address_line_1'),
        (9046, 'employer_address_line_2'),
        (9047, 'employer_address_line_3'),
        (9048, 'employer_address_line_4'),
        (9049, 'employer_address_line_5'),
        (9050, 'employer_postcode'),
        (9051, 'employer_telephone'),
        (9052, 'employer_email');

    -- Insert test audit amendment fields for creditor accounts
    INSERT INTO audit_amendment_fields (field_code, data_item) VALUES
        (9060, 'hold_payout'),
        (9061, 'pay_by_bacs'),
        (9062, 'bank_sort_code'),
        (9063, 'bank_account_type'),
        (9064, 'bank_account_number'),
        (9065, 'bank_account_name'),
        (9066, 'bank_account_reference');

    RAISE NOTICE 'Test audit amendment fields data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Test defendant_accounts amendment detection with changes
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90001;
    v_business_unit_id           SMALLINT := 65;
    v_party_id_def               BIGINT;
    v_party_id_pg                BIGINT;
    v_amendment_count            INTEGER;
    v_last_changed_before        TIMESTAMP;
    v_last_changed_after         TIMESTAMP;
BEGIN
    RAISE NOTICE '=== TEST 1: Test defendant_accounts amendment detection with changes ===';
    
    -- Setup test data - Create defendant party
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, 
        address_line_1, address_line_2, address_line_3, postcode,
        birth_date, national_insurance_number, telephone_home, 
        telephone_business, telephone_mobile, email_1, email_2
    ) VALUES (
        90001, FALSE, 'TestDefendant', 'John', 'Mr',
        '123 Test Street', 'Test Area', 'Test City', 'TE1 2ST',
        '1980-01-01'::DATE, 'AB123456C', '01234567890',
        '01234567891', '07123456789', 'john.test@example.com', 'j.test@example.com'
    ) RETURNING party_id INTO v_party_id_def;

    -- Create parent/guardian party
    INSERT INTO parties (
        party_id, organisation, surname, forenames, 
        address_line_1, address_line_2, address_line_3, postcode,
        birth_date, national_insurance_number
    ) VALUES (
        90002, FALSE, 'TestParent', 'Jane',
        '456 Parent Street', '', '', 'TE2 3ST',
        '1950-01-01'::DATE, 'CD123456E'
    ) RETURNING party_id INTO v_party_id_pg;

    -- Create defendant account
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, cheque_clearance_period, allow_cheques,
        credit_trans_clearance_period, allow_writeoffs, enforcing_court_id,
        collection_order, suspended_committal_date, account_comments,
        account_note_1, account_note_2, account_note_3, last_changed_date
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST001', 'Fine',
        '2024-01-01'::DATE, 100.00, 50.00, 50.00,
        'L', 5, TRUE, 3, TRUE, 650000000045,
        TRUE, '2024-12-15'::DATE, 'Original comments',
        'Test note 1', 'Test note 2', 'Test note 3', '2024-01-01 10:00:00'::TIMESTAMP
    );

    -- Store the original last_changed_date
    SELECT last_changed_date INTO v_last_changed_before 
    FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id;

    -- Create defendant account parties associations
    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES 
        (90001, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE),
        (90002, v_defendant_account_id, v_party_id_pg, 'Parent/Guardian', TRUE);

    -- Create debtor details
    INSERT INTO debtor_detail (
        party_id, vehicle_make, vehicle_registration, employer_name,
        employer_address_line_1, employer_address_line_2, employer_address_line_3,
        employer_address_line_4, employer_address_line_5, employer_postcode,
        employee_reference, employer_telephone, employer_email,
        document_language, document_language_date, hearing_language, hearing_language_date
    ) VALUES 
        (v_party_id_def, 'Honda', 'ABC123', 'Test Corp',
         '789 Business Rd', 'Floor 2', '', '', '', 'TE3 4ST',
         'EMP123', '01234567892', 'hr@testcorp.com',
         'EN', CURRENT_TIMESTAMP, 'EN', CURRENT_TIMESTAMP);

    -- Create aliases
    INSERT INTO aliases (alias_id, party_id, surname, forenames, sequence_number, organisation_name)
    VALUES 
        (90001, v_party_id_def, 'TestAlias1', 'Johnny', 1, ''),
        (90002, v_party_id_def, 'TestAlias2', 'Jon', 2, '');

    -- Create temporary table with initial values (simulating what p_audit_initialise would do)
    CREATE TEMP TABLE temp_def_ac_amendment_list AS
    SELECT 
        da.defendant_account_id,
        da.cheque_clearance_period,
        da.allow_cheques,
        da.credit_trans_clearance_period,
        da.allow_writeoffs,
        da.enf_override_enforcer_id,
        da.enf_override_result_id,
        da.enf_override_tfo_lja_id,
        da.enforcing_court_id,
        da.collection_order,
        da.suspended_committal_date,
        da.account_comments,
        da.account_note_1,
        da.account_note_2,
        da.account_note_3,
        COALESCE(p_def.title || ' ', '') || COALESCE(p_def.forenames || ' ', '') || COALESCE(p_def.surname, '') AS name,
        p_def.birth_date,
        EXTRACT(YEAR FROM age(p_def.birth_date)) AS age,
        p_def.address_line_1,
        p_def.address_line_2,
        p_def.address_line_3,
        p_def.postcode,
        p_def.national_insurance_number,
        p_def.telephone_home,
        p_def.telephone_business,
        p_def.telephone_mobile,
        p_def.email_1,
        p_def.email_2,
        COALESCE(p_pg.title || ' ', '') || COALESCE(p_pg.forenames || ' ', '') || COALESCE(p_pg.surname, '') AS pname,
        p_pg.address_line_1 AS paddr1,
        p_pg.address_line_2 AS paddr2,
        p_pg.address_line_3 AS paddr3,
        p_pg.birth_date AS pbdate,
        p_pg.national_insurance_number AS pninumber,
        a1.surname AS alias1,
        a2.surname AS alias2,
        a3.surname AS alias3,
        a4.surname AS alias4,
        a5.surname AS alias5,
        dd.document_language,
        dd.hearing_language,
        dd.vehicle_make,
        dd.vehicle_registration,
        dd.employee_reference,
        dd.employer_name,
        dd.employer_address_line_1,
        dd.employer_address_line_2,
        dd.employer_address_line_3,
        dd.employer_address_line_4,
        dd.employer_address_line_5,
        dd.employer_postcode,
        dd.employer_telephone,
        dd.employer_email
    FROM defendant_accounts da
    LEFT JOIN defendant_account_parties dap_def ON da.defendant_account_id = dap_def.defendant_account_id 
        AND dap_def.association_type = 'Defendant'
    LEFT JOIN parties p_def ON dap_def.party_id = p_def.party_id
    LEFT JOIN defendant_account_parties dap_pg ON da.defendant_account_id = dap_pg.defendant_account_id 
        AND dap_pg.association_type = 'Parent/Guardian'
    LEFT JOIN parties p_pg ON dap_pg.party_id = p_pg.party_id
    LEFT JOIN debtor_detail dd ON p_def.party_id = dd.party_id
    LEFT JOIN aliases a1 ON p_def.party_id = a1.party_id AND a1.sequence_number = 1
    LEFT JOIN aliases a2 ON p_def.party_id = a2.party_id AND a2.sequence_number = 2
    LEFT JOIN aliases a3 ON p_def.party_id = a3.party_id AND a3.sequence_number = 3
    LEFT JOIN aliases a4 ON p_def.party_id = a4.party_id AND a4.sequence_number = 4
    LEFT JOIN aliases a5 ON p_def.party_id = a5.party_id AND a5.sequence_number = 5
    WHERE da.defendant_account_id = v_defendant_account_id;

    -- Fields that trigger last_changed_date: 'name', 'birth_date', 'address_line_1', 'postcode', 'alias1', 'alias2', 'alias3', 'alias4', 'alias5'
    -- Update fields that trigger last_changed_date update (name via title change)
    UPDATE parties 
    SET title = 'Dr',  -- This will change the 'name' field
        birth_date = '1981-01-01'::DATE,
        address_line_1 = 'Updated Address',
        postcode = 'TE1 3ST'
    WHERE party_id = v_party_id_def;

    UPDATE aliases 
    SET surname = 'UpdatedAlias1'
    WHERE alias_id = 90001;

    -- Call the finalise procedure
    CALL p_audit_finalise(
        v_defendant_account_id, 
        'defendant_accounts',
        v_business_unit_id,
        'TEST_USER',
        'CASE123',
        'ACCOUNT_ENQUIRY'
    );

    -- Verify amendments were created
    SELECT COUNT(*) INTO v_amendment_count 
    FROM amendments 
    WHERE associated_record_type = 'defendant_accounts' 
    AND associated_record_id = v_defendant_account_id::VARCHAR;

    ASSERT v_amendment_count >= 5, 'Should have created at least 5 amendment records for the changes made';

    -- Verify last_changed_date was updated
    SELECT last_changed_date INTO v_last_changed_after 
    FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_last_changed_after > v_last_changed_before, 'last_changed_date should have been updated due to changes in name, birth_date, address_line_1, postcode, and alias1';

    -- Verify temporary table was cleaned up
    ASSERT NOT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'temp_def_ac_amendment_list'
        AND table_type = 'LOCAL TEMPORARY'
    ), 'Temporary table should have been cleaned up';

    RAISE NOTICE 'TEST 1 PASSED: Defendant accounts amendment detection works correctly';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Test creditor_accounts amendment detection with changes
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_creditor_account_id        BIGINT := 90003;
    v_business_unit_id           SMALLINT := 65;
    v_party_id                   BIGINT;
    v_amendment_count            INTEGER;
    v_last_changed_before        TIMESTAMP;
    v_last_changed_after         TIMESTAMP;
BEGIN
    RAISE NOTICE '=== TEST 2: Test creditor_accounts amendment detection with changes ===';
    
    -- Setup test data
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, address_line_1, address_line_2, 
        address_line_3, postcode
    ) VALUES (
        90003, FALSE, 'TestCreditor', 'Jane', 'Ms', '789 Creditor St', 'Creditor Area', 
        'Creditor City', 'CR1 2ED'
    ) RETURNING party_id INTO v_party_id;

    -- Create creditor account
    INSERT INTO creditor_accounts (
        creditor_account_id, business_unit_id, account_number, 
        creditor_account_type, minor_creditor_party_id, prosecution_service,
        from_suspense, hold_payout, pay_by_bacs, bank_sort_code, 
        bank_account_type, bank_account_number, bank_account_name, 
        bank_account_reference, last_changed_date
    ) VALUES (
        v_creditor_account_id, v_business_unit_id, 'TESTCRED001',
        'MN', v_party_id, FALSE,
        FALSE, FALSE, TRUE, '123456',
        '1', '12345678', 'TestCredAcct', 'REF123', '2024-01-01 10:00:00'::TIMESTAMP
    );

    -- Store the original last_changed_date
    SELECT last_changed_date INTO v_last_changed_before 
    FROM creditor_accounts WHERE creditor_account_id = v_creditor_account_id;

    -- Create temporary table with initial values (simulating what p_audit_initialise would do)
    CREATE TEMP TABLE temp_cred_ac_amendment_list AS
    SELECT 
        ca.creditor_account_id,
        ca.hold_payout,
        ca.pay_by_bacs,
        ca.bank_sort_code,
        ca.bank_account_type,
        ca.bank_account_number,
        ca.bank_account_name,
        ca.bank_account_reference,
        COALESCE(p.title || ' ', '') || COALESCE(p.forenames || ' ', '') || COALESCE(p.surname, '') AS name,
        p.address_line_1,
        p.address_line_2,
        p.address_line_3,
        p.postcode
    FROM creditor_accounts ca
    LEFT JOIN parties p ON ca.minor_creditor_party_id = p.party_id
    WHERE ca.creditor_account_id = v_creditor_account_id;

    -- Make changes to tracked fields including fields that trigger last_changed_date update
    -- Fields that trigger last_changed_date for creditor_accounts: 'name', 'address_line_1', 'address_line_2', 'address_line_3', 'postcode', 'hold_payout', 'pay_by_bacs', 'bank_sort_code', 'bank_account_type', 'bank_account_number', 'bank_account_name', 'bank_account_reference'
    UPDATE creditor_accounts 
    SET hold_payout = TRUE,
        bank_sort_code = '654321',
        bank_account_name = 'UpdatedCredAcct'
    WHERE creditor_account_id = v_creditor_account_id;

    -- Update party fields that trigger last_changed_date update
    UPDATE parties 
    SET title = 'Dr',  -- This will change the 'name' field, triggers last_changed_date update
        address_line_1 = 'Updated Creditor Address',
        address_line_2 = 'Updated Area',
        postcode = 'CR2 3ED'
    WHERE party_id = v_party_id;

    -- Call the finalise procedure
    CALL p_audit_finalise(
        v_creditor_account_id, 
        'creditor_accounts',
        v_business_unit_id,
        'TEST_USER',
        'CASE456',
        'CREDITOR_MAINTENANCE'
    );

    -- Verify amendments were created
    SELECT COUNT(*) INTO v_amendment_count 
    FROM amendments 
    WHERE associated_record_type = 'creditor_accounts' 
    AND associated_record_id = v_creditor_account_id::VARCHAR;

    ASSERT v_amendment_count >= 6, 'Should have created at least 6 amendment records for the changes made';

    -- Verify specific amendments exist
    PERFORM 1 FROM amendments 
    WHERE associated_record_id = v_creditor_account_id::VARCHAR
    AND field_code = (SELECT field_code FROM audit_amendment_fields WHERE data_item = 'hold_payout')
    AND old_value = 'false'
    AND new_value = 'true'
    AND function_code = 'CREDITOR_MAINTENANCE';

    ASSERT FOUND, 'Should have amendment record for hold_payout change';

    PERFORM 1 FROM amendments 
    WHERE associated_record_id = v_creditor_account_id::VARCHAR
    AND field_code = (SELECT field_code FROM audit_amendment_fields WHERE data_item = 'bank_sort_code')
    AND old_value = '123456'
    AND new_value = '654321';

    ASSERT FOUND, 'Should have amendment record for bank_sort_code change';

    -- Verify last_changed_date was updated
    SELECT last_changed_date INTO v_last_changed_after 
    FROM creditor_accounts WHERE creditor_account_id = v_creditor_account_id;

    ASSERT v_last_changed_after > v_last_changed_before, 'last_changed_date should have been updated due to changes in fields that trigger update';

    -- Verify temporary table was cleaned up
    ASSERT NOT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'temp_cred_ac_amendment_list'
        AND table_type = 'LOCAL TEMPORARY'
    ), 'Temporary table should have been cleaned up';

    RAISE NOTICE 'TEST 2 PASSED: Creditor accounts amendment detection works correctly';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 3: Test no amendments when no changes made
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_defendant_account_id       BIGINT := 90004;
    v_business_unit_id           SMALLINT := 65;
    v_party_id_def               BIGINT;
    v_amendment_count            INTEGER;
    v_last_changed_before        TIMESTAMP;
    v_last_changed_after         TIMESTAMP;
BEGIN
    RAISE NOTICE '=== TEST 3: Test no amendments when no changes made ===';
    
    -- Setup test data - Create defendant party
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, 
        address_line_1, address_line_2, address_line_3, postcode,
        birth_date, national_insurance_number
    ) VALUES (
        90004, FALSE, 'TestDefendant2', 'John', 'Mr',
        '123 Test Street', 'Test Area', 'Test City', 'TE1 2ST',
        '1980-01-01'::DATE, 'AB123456C'
    ) RETURNING party_id INTO v_party_id_def;

    -- Create defendant account
    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, cheque_clearance_period, allow_cheques,
        credit_trans_clearance_period, allow_writeoffs, enforcing_court_id,
        collection_order, suspended_committal_date, account_comments,
        account_note_1, account_note_2, account_note_3, last_changed_date
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST002', 'Fine',
        '2024-01-01'::DATE, 100.00, 50.00, 50.00,
        'L', 5, TRUE, 3, TRUE, 650000000045,
        TRUE, '2024-12-15'::DATE, 'Test comments',
        'Test note 1', 'Test note 2', 'Test note 3', '2024-01-01 10:00:00'::TIMESTAMP
    );

    -- Store the original last_changed_date
    SELECT last_changed_date INTO v_last_changed_before 
    FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id;

    -- Create defendant account parties association
    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES 
        (90003, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Create temporary table with initial values (simulating what p_audit_initialise would do)
    CREATE TEMP TABLE temp_def_ac_amendment_list AS
    SELECT 
        da.defendant_account_id,
        da.cheque_clearance_period,
        da.allow_cheques,
        da.credit_trans_clearance_period,
        da.allow_writeoffs,
        da.enf_override_enforcer_id,
        da.enf_override_result_id,
        da.enf_override_tfo_lja_id,
        da.enforcing_court_id,
        da.collection_order,
        da.suspended_committal_date,
        da.account_comments,
        da.account_note_1,
        da.account_note_2,
        da.account_note_3,
        COALESCE(p_def.title || ' ', '') || COALESCE(p_def.forenames || ' ', '') || COALESCE(p_def.surname, '') AS name,
        p_def.birth_date,
        p_def.age,
        p_def.address_line_1,
        p_def.address_line_2,
        p_def.address_line_3,
        p_def.postcode,
        p_def.national_insurance_number,
        p_def.telephone_home,
        p_def.telephone_business,
        p_def.telephone_mobile,
        p_def.email_1,
        p_def.email_2,
        '' AS pname,
        '' AS paddr1,
        '' AS paddr2,
        '' AS paddr3,
        NULL::DATE AS pbdate,
        '' AS pninumber,
        '' AS alias1,
        '' AS alias2,
        '' AS alias3,
        '' AS alias4,
        '' AS alias5,
        '' AS document_language,
        '' AS hearing_language,
        '' AS vehicle_make,
        '' AS vehicle_registration,
        '' AS employee_reference,
        '' AS employer_name,
        '' AS employer_address_line_1,
        '' AS employer_address_line_2,
        '' AS employer_address_line_3,
        '' AS employer_address_line_4,
        '' AS employer_address_line_5,
        '' AS employer_postcode,
        '' AS employer_telephone,
        '' AS employer_email
    FROM defendant_accounts da
    LEFT JOIN defendant_account_parties dap_def ON da.defendant_account_id = dap_def.defendant_account_id 
        AND dap_def.association_type = 'Defendant'
    LEFT JOIN parties p_def ON dap_def.party_id = p_def.party_id
    WHERE da.defendant_account_id = v_defendant_account_id;

    -- Don't make any changes - just call finalise
    CALL p_audit_finalise(
        v_defendant_account_id, 
        'defendant_accounts',
        v_business_unit_id,
        'TEST_USER',
        NULL,
        'ACCOUNT_ENQUIRY'
    );

    -- Verify no amendments were created
    SELECT COUNT(*) INTO v_amendment_count 
    FROM amendments 
    WHERE associated_record_type = 'defendant_accounts' 
    AND associated_record_id = v_defendant_account_id::VARCHAR;

    ASSERT v_amendment_count = 0, 'Should have created 0 amendment records when no changes made';

    -- Verify last_changed_date was NOT updated
    SELECT last_changed_date INTO v_last_changed_after 
    FROM defendant_accounts WHERE defendant_account_id = v_defendant_account_id;

    ASSERT v_last_changed_after = v_last_changed_before, 'last_changed_date should not have been updated when no fields that trigger update were changed';

    RAISE NOTICE 'TEST 3 PASSED: No amendments created when no changes made';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 4: Test error handling - NULL associated_account_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3005';
    v_expected_message           VARCHAR := 'Associated account ID cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 4: Test error handling - NULL associated_account_id ===';
    
    -- Call the procedure with NULL account ID - should throw P3005 exception
    BEGIN
        CALL p_audit_finalise(
            NULL::BIGINT, 
            'defendant_accounts'::VARCHAR, 
            65::SMALLINT, 
            'TEST_USER'::VARCHAR, 
            NULL::VARCHAR, 
            'TEST'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3005' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3005 error should have been raised due to NULL associated_account_id';

    RAISE NOTICE 'TEST 4 PASSED: Error handling works correctly for NULL associated_account_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Test error handling - Invalid record type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3006';
BEGIN
    RAISE NOTICE '=== TEST 5: Test error handling - Invalid record type ===';
    
    -- Call the procedure with invalid record type - should throw P3006 exception
    BEGIN
        CALL p_audit_finalise(
            12345::BIGINT, 
            'invalid_type'::VARCHAR, 
            65::SMALLINT, 
            'TEST_USER'::VARCHAR, 
            NULL::VARCHAR, 
            'TEST'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3006' THEN
            IF SQLERRM LIKE '%Invalid record type%' THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3006 error should have been raised due to invalid record type';

    RAISE NOTICE 'TEST 5 PASSED: Error handling works correctly for invalid record type';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Test error handling - NULL business_unit_id
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3007';
    v_expected_message           VARCHAR := 'Business unit ID cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 6: Test error handling - NULL business_unit_id ===';
    
    -- Call the procedure with NULL business_unit_id - should throw P3007 exception
    BEGIN
        CALL p_audit_finalise(
            12345::BIGINT, 
            'defendant_accounts'::VARCHAR, 
            NULL::SMALLINT, 
            'TEST_USER'::VARCHAR, 
            NULL::VARCHAR, 
            'TEST'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3007' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3007 error should have been raised due to NULL business_unit_id';

    RAISE NOTICE 'TEST 6 PASSED: Error handling works correctly for NULL business_unit_id';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: Test error handling - NULL posted_by
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3008';
    v_expected_message           VARCHAR := 'Posted by cannot be null';
BEGIN
    RAISE NOTICE '=== TEST 7: Test error handling - NULL posted_by ===';
    
    -- Call the procedure with NULL posted_by - should throw P3008 exception
    BEGIN
        CALL p_audit_finalise(
            12345::BIGINT, 
            'defendant_accounts'::VARCHAR, 
            65::SMALLINT, 
            NULL::VARCHAR, 
            NULL::VARCHAR, 
            'TEST'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3008' THEN
            IF SQLERRM = v_expected_message THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3008 error should have been raised due to NULL posted_by';

    RAISE NOTICE 'TEST 7 PASSED: Error handling works correctly for NULL posted_by';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 8: Test error handling - Missing temporary table
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3009';
    v_defendant_account_id       BIGINT := 90005;
    v_business_unit_id           SMALLINT := 65;
    v_party_id_def               BIGINT;
BEGIN
    RAISE NOTICE '=== TEST 8: Test error handling - Missing temporary table ===';
    
    -- Setup minimal test data
    INSERT INTO parties (
        party_id, organisation, surname, forenames, title, 
        address_line_1, postcode, birth_date
    ) VALUES (
        90005, FALSE, 'TestDefendant3', 'John', 'Mr',
        '123 Test Street', 'TE1 2ST', '1980-01-01'::DATE
    ) RETURNING party_id INTO v_party_id_def;

    INSERT INTO defendant_accounts (
        defendant_account_id, business_unit_id, account_number, account_type,
        imposed_hearing_date, amount_imposed, amount_paid, account_balance,
        account_status, cheque_clearance_period, allow_cheques,
        credit_trans_clearance_period, allow_writeoffs
    ) VALUES (
        v_defendant_account_id, v_business_unit_id, 'TEST003', 'Fine',
        '2024-01-01'::DATE, 100.00, 50.00, 50.00,
        'L', 5, TRUE, 3, TRUE
    );

    INSERT INTO defendant_account_parties (
        defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
    ) VALUES 
        (90004, v_defendant_account_id, v_party_id_def, 'Defendant', FALSE);

    -- Call finalise without creating temporary table first - should throw P3009 exception
    BEGIN
        CALL p_audit_finalise(
            v_defendant_account_id, 
            'defendant_accounts',
            v_business_unit_id,
            'TEST_USER',
            NULL,
            'TEST'
        );
    EXCEPTION
        WHEN SQLSTATE 'P3009' THEN
            IF SQLERRM LIKE '%Temporary table%does not exist%' THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3009 error should have been raised due to missing temporary table';

    RAISE NOTICE 'TEST 8 PASSED: Error handling works correctly for missing temporary table';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 9: Test error handling - Non-existent defendant account ID
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_error_caught               BOOLEAN := FALSE;
    v_expected_sqlstate          VARCHAR := 'P3010';
    v_non_existent_id            BIGINT := 999999;
BEGIN
    RAISE NOTICE '=== TEST 9: Test error handling - Non-existent defendant account ID ===';
    
    -- Create a fake temporary table to bypass the temp table check
    CREATE TEMP TABLE temp_def_ac_amendment_list AS 
    SELECT 999999::BIGINT as defendant_account_id, 'fake'::VARCHAR as name;

    -- Call the procedure with non-existent defendant account ID - should throw P3010 exception
    BEGIN
        CALL p_audit_finalise(
            v_non_existent_id::BIGINT, 
            'defendant_accounts'::VARCHAR,
            65::SMALLINT,
            'TEST_USER'::VARCHAR,
            NULL::VARCHAR,
            'TEST'::VARCHAR
        );
    EXCEPTION
        WHEN SQLSTATE 'P3010' THEN
            IF SQLERRM LIKE '%No defendant account found with ID%' THEN
                v_error_caught := TRUE;
                RAISE NOTICE 'Expected error caught: % - %', SQLSTATE, SQLERRM;
            ELSE 
                RAISE WARNING 'Expected error SQLSTATE caught but with wrong SQLERRM: % - %', SQLSTATE, SQLERRM;
            END IF;
        WHEN OTHERS THEN
            v_error_caught := FALSE;
            RAISE NOTICE 'Unexpected error caught: % - %', SQLSTATE, SQLERRM;
    END;

    -- Verify error was caught
    ASSERT v_error_caught = TRUE, 'A P3010 error should have been raised due to non-existent defendant account ID';

    -- Clean up fake temp table
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;

    RAISE NOTICE 'TEST 9 PASSED: Error handling works correctly for non-existent defendant account ID';
END $$;

-- Cleanup test data
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';
    
    -- Delete test data from related tables in correct order to avoid FK violations
    DELETE FROM amendments WHERE associated_record_id IN ('90001', '90003', '90004', '90005');
    DELETE FROM aliases WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3'));
    DELETE FROM debtor_detail WHERE party_id IN (SELECT party_id FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3'));
    DELETE FROM defendant_account_parties WHERE defendant_account_id IN (SELECT defendant_account_id FROM defendant_accounts WHERE account_number LIKE 'TEST%');
    DELETE FROM creditor_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM defendant_accounts WHERE account_number LIKE 'TEST%';
    DELETE FROM parties WHERE surname IN ('TestDefendant', 'TestCreditor', 'TestParent', 'TestDefendant2', 'TestDefendant3');
    
    -- Delete test audit amendment fields
    DELETE FROM audit_amendment_fields WHERE field_code BETWEEN 9000 AND 9999;

    -- Drop temporary tables
    DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
    
    RAISE NOTICE 'Test data cleanup completed';
END $$;

\timing