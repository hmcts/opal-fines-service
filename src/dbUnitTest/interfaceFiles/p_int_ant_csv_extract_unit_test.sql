\set ON_ERROR_STOP on

/**
* CGI Opal Program
*
* MODULE      : p_int_ant_csv_extract_unit_test
*
* DESCRIPTION : Unit tests for the p_int_ant_csv_extract stored procedure.
*               These tests cover the procedure signature, APR filtering,
*               output mapping and ordering, including a single-element APR array.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------------
* 13/11/2026    TT          1.0         PO-11448 - Unit tests for p_int_ant_csv_extract.
*
**/

BEGIN;

DO LANGUAGE plpgsql $$
DECLARE
    c_area_business_unit_id       CONSTANT SMALLINT := 9900;
    c_first_business_unit_id      CONSTANT SMALLINT := 9901;
    c_single_apr_business_unit_id CONSTANT SMALLINT := 9902;
    c_zero_business_unit_id       CONSTANT SMALLINT := 9903;
    c_area_business_unit_code     CONSTANT VARCHAR(4) := '9980';
    c_first_business_unit_code    CONSTANT VARCHAR(4) := '9981';
    c_single_apr_business_unit_code CONSTANT VARCHAR(4) := '9982';
    c_zero_business_unit_code     CONSTANT VARCHAR(4) := '9983';
    c_first_account_id            CONSTANT BIGINT := 990000000071001;
    c_single_apr_account_id       CONSTANT BIGINT := 990000000071002;
    c_zero_account_id             CONSTANT BIGINT := 990000000071003;
    c_first_account_number        CONSTANT VARCHAR(20) := 'EI8-TEST-9901';
    c_single_apr_account_number   CONSTANT VARCHAR(20) := 'EI8-TEST-9902';
    c_zero_account_number         CONSTANT VARCHAR(20) := 'EI8-TEST-9903';
    c_first_apr_text              CONSTANT VARCHAR := 'EI8-TEST-9901-A';
    c_second_apr_text             CONSTANT VARCHAR := 'EI8-TEST-9901-B';
    c_single_apr_text             CONSTANT VARCHAR := 'EI8-TEST-9902-A';
    c_zero_apr_text               CONSTANT VARCHAR := 'EI8-TEST-9903-ZERO';
    v_procedure_output            JSON[];
    v_item                        JSON;
    v_apr_a                       JSON;
    v_apr_b                       JSON;
    v_single_apr                  JSON;
    v_count                       BIGINT;
    v_first_bu_ordinal            BIGINT;
    v_second_bu_ordinal           BIGINT;
    v_first_apr_ordinal           BIGINT;
    v_second_apr_ordinal          BIGINT;
    v_error_state                 TEXT;
    v_error_message               TEXT;
    v_failed_test_count           INTEGER := 0;
BEGIN
    RAISE NOTICE '=== Setting up p_int_ant_csv_extract test data ===';

    ASSERT NOT EXISTS (
        SELECT 1
        FROM business_units
        WHERE business_unit_id IN (
            c_area_business_unit_id,
            c_first_business_unit_id,
            c_single_apr_business_unit_id,
            c_zero_business_unit_id
        )
    ), 'One or more test business_unit_id values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1
        FROM business_units
        WHERE business_unit_code IN (
            c_area_business_unit_code,
            c_first_business_unit_code,
            c_single_apr_business_unit_code,
            c_zero_business_unit_code
        )
    ), 'One or more test business_unit_code values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1
        FROM defendant_accounts
        WHERE defendant_account_id IN (
            c_first_account_id,
            c_single_apr_account_id,
            c_zero_account_id
        )
    ), 'One or more test defendant_account_id values are already in use';

    ASSERT NOT EXISTS (
        SELECT 1
        FROM alternate_payment_references
        WHERE apr_text IN (
            c_first_apr_text,
            c_second_apr_text,
            c_single_apr_text,
            c_zero_apr_text
        )
    ), 'One or more test APR labels are already in use';

    INSERT INTO business_units (
        business_unit_id,
        business_unit_name,
        business_unit_code,
        business_unit_type,
        parent_business_unit_id
    ) VALUES
        (c_area_business_unit_id, 'EI8 test Area', c_area_business_unit_code, 'Area'::t_business_unit_type_enum, NULL),
        (c_first_business_unit_id, 'EI8 test BU with two APRs', c_first_business_unit_code, 'Accounting Division'::t_business_unit_type_enum, c_area_business_unit_id),
        (c_single_apr_business_unit_id, 'EI8 test BU with one APR', c_single_apr_business_unit_code, 'Accounting Division'::t_business_unit_type_enum, c_area_business_unit_id),
        (c_zero_business_unit_id, 'EI8 test zero-balance BU', c_zero_business_unit_code, 'Accounting Division'::t_business_unit_type_enum, c_area_business_unit_id);

    INSERT INTO defendant_accounts (
        defendant_account_id,
        business_unit_id,
        account_number,
        amount_imposed,
        amount_paid,
        account_balance,
        account_status,
        account_type
    ) VALUES
        (c_first_account_id, c_first_business_unit_id, c_first_account_number, 100.00, 0.00, 100.00,
         'L'::t_da_account_status_enum, 'Fine'::t_da_account_type_enum),
        (c_single_apr_account_id, c_single_apr_business_unit_id, c_single_apr_account_number, 100.00, 0.00, 100.00,
         'L'::t_da_account_status_enum, 'Fine'::t_da_account_type_enum),
        (c_zero_account_id, c_zero_business_unit_id, c_zero_account_number, 100.00, 100.00, 0.00,
         'CS'::t_da_account_status_enum, 'Fine'::t_da_account_type_enum);

    INSERT INTO alternate_payment_references (
        defendant_account_id,
        relationship,
        category,
        business_unit_id,
        apr_text,
        created_datetime,
        updated_datetime
    ) VALUES
        (c_first_account_id, 'APR'::t_apr_relationship_enum, 'APR'::t_apr_category_enum,
         c_first_business_unit_id, c_first_apr_text, TIMESTAMP '2026-01-02 12:00:00', TIMESTAMP '2026-01-03 12:00:00'),
        (c_first_account_id, 'CONSOLIDATED'::t_apr_relationship_enum, 'ACC'::t_apr_category_enum,
         c_first_business_unit_id, c_second_apr_text, TIMESTAMP '2026-01-04 12:00:00', TIMESTAMP '2026-01-05 12:00:00'),
        (c_single_apr_account_id, 'APR'::t_apr_relationship_enum, 'APR'::t_apr_category_enum,
         c_single_apr_business_unit_id, c_single_apr_text, TIMESTAMP '2026-02-02 12:00:00', TIMESTAMP '2026-02-03 12:00:00'),
        (c_zero_account_id, 'APR'::t_apr_relationship_enum, 'APR'::t_apr_category_enum,
         c_zero_business_unit_id, c_zero_apr_text, TIMESTAMP '2026-03-02 12:00:00', TIMESTAMP '2026-03-03 12:00:00');

    RAISE NOTICE '=== Test data setup completed ===';

    RAISE NOTICE '=== TEST 1: Procedure exists with the required OUT signature ===';
    BEGIN
        ASSERT EXISTS (
            SELECT 1
            FROM pg_proc p
            JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE n.nspname = current_schema()
              AND p.proname = 'p_int_ant_csv_extract'
              AND p.prokind = 'p'
        ), 'p_int_ant_csv_extract does not exist as a procedure';

        ASSERT EXISTS (
            SELECT 1
            FROM pg_proc p
            JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE n.nspname = current_schema()
              AND p.proname = 'p_int_ant_csv_extract'
              AND p.prokind = 'p'
              AND p.pronargs = 0
              AND array_length(p.proallargtypes, 1) = 1
              AND p.proallargtypes[1] = 'json[]'::regtype::oid
              AND p.proargnames[1] = 'po_apr_details_json'
        ), 'procedure signature is not no-input OUT po_apr_details_json JSON[]';

        RAISE NOTICE 'TEST 1 PASSED';
    EXCEPTION
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 1 FAILED [%]: %', v_error_state, v_error_message;
    END;

    RAISE NOTICE '=== TEST 2: Qualifying APR groups, filtering, and ordering ===';
    BEGIN
        CALL p_int_ant_csv_extract(v_procedure_output);

        ASSERT (
            SELECT count(*)
            FROM unnest(v_procedure_output) AS result(item)
            WHERE result.item->>'business_unit_id' IN (
                c_first_business_unit_id::text,
                c_single_apr_business_unit_id::text,
                c_zero_business_unit_id::text
            )
        ) = 2,
            'Expected exactly two qualifying test BU output entries';

        SELECT min(result.ordinality) FILTER (WHERE result.item->>'business_unit_id' = c_first_business_unit_id::text),
               min(result.ordinality) FILTER (WHERE result.item->>'business_unit_id' = c_single_apr_business_unit_id::text)
        INTO v_first_bu_ordinal, v_second_bu_ordinal
        FROM unnest(v_procedure_output) WITH ORDINALITY AS result(item, ordinality);
        ASSERT v_first_bu_ordinal IS NOT NULL
           AND v_second_bu_ordinal IS NOT NULL
           AND v_first_bu_ordinal < v_second_bu_ordinal,
            'Business-unit output entries are not ordered by ascending business_unit_id';

        ASSERT NOT EXISTS (
            SELECT 1
            FROM unnest(v_procedure_output) AS result(item)
            WHERE result.item->>'business_unit_id' = c_zero_business_unit_id::text
        ), 'A zero-balance-only business unit was returned';

        SELECT result.item
        INTO v_item
        FROM unnest(v_procedure_output) AS result(item)
        WHERE result.item->>'business_unit_id' = c_first_business_unit_id::text;
        ASSERT json_array_length(v_item->'apr_records') = 2,
            'BU 9901 should contain two APR records';

        SELECT result.item
        INTO v_item
        FROM unnest(v_procedure_output) AS result(item)
        WHERE result.item->>'business_unit_id' = c_single_apr_business_unit_id::text;
        ASSERT json_array_length(v_item->'apr_records') = 1,
            'BU 9902 should contain a single-element APR array';

        RAISE NOTICE 'TEST 2 PASSED';
    EXCEPTION
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 2 FAILED [%]: %', v_error_state, v_error_message;
    END;

    RAISE NOTICE '=== TEST 3: APR field mappings and date formats ===';
    BEGIN
        SELECT apr.record
        INTO v_apr_a
        FROM unnest(v_procedure_output) AS result(item)
        CROSS JOIN LATERAL json_array_elements(result.item->'apr_records') AS apr(record)
        WHERE result.item->>'business_unit_id' = c_first_business_unit_id::text
          AND apr.record->>'child_account_or_apr' = c_first_apr_text;

        ASSERT v_apr_a->>'accounting_division' = '99809901', 'BU 9901 accounting_division is incorrect';
        ASSERT v_apr_a->>'category' = 'APR', 'APR text ''EI8-TEST-9901-A'' category is incorrect';
        ASSERT v_apr_a->>'parent_account' = c_first_account_number, 'APR text ''EI8-TEST-9901-A'' parent account is incorrect';
        ASSERT v_apr_a->>'parent_category' = 'ACC', 'APR text ''EI8-TEST-9901-A'' parent category is incorrect';
        ASSERT v_apr_a->>'closed_status' = 'N', 'APR text ''EI8-TEST-9901-A'' closed_status is incorrect';
        ASSERT (v_apr_a::jsonb ? 'date_close_status')
           AND v_apr_a->>'date_close_status' IS NULL,
            'APR text ''EI8-TEST-9901-A'' date_close_status should be present as JSON null';
        ASSERT v_apr_a->>'insert_date' = '26002', 'APR text ''EI8-TEST-9901-A'' insert_date should use YYDDD';
        ASSERT v_apr_a->>'update_date' = '26003', 'APR text ''EI8-TEST-9901-A'' update_date should use YYDDD';
        ASSERT v_apr_a->>'status' = 'A', 'APR text ''EI8-TEST-9901-A'' status is incorrect';
        ASSERT v_apr_a->>'relationship' = 'APR', 'APR text ''EI8-TEST-9901-A'' relationship is incorrect';

        SELECT apr.record
        INTO v_apr_b
        FROM unnest(v_procedure_output) AS result(item)
        CROSS JOIN LATERAL json_array_elements(result.item->'apr_records') AS apr(record)
        WHERE result.item->>'business_unit_id' = c_first_business_unit_id::text
          AND apr.record->>'child_account_or_apr' = c_second_apr_text;

        ASSERT v_apr_b->>'category' = 'ACC', 'APR text ''EI8-TEST-9901-B'' category should map to ACC';
        ASSERT v_apr_b->>'relationship' = 'CON', 'APR text ''EI8-TEST-9901-B'' relationship should be truncated to CON';
        ASSERT v_apr_b->>'insert_date' = '26004', 'APR text ''EI8-TEST-9901-B'' insert_date should use YYDDD';
        ASSERT v_apr_b->>'update_date' = '26005', 'APR text ''EI8-TEST-9901-B'' update_date should use YYDDD';

        SELECT apr.record
        INTO v_single_apr
        FROM unnest(v_procedure_output) AS result(item)
        CROSS JOIN LATERAL json_array_elements(result.item->'apr_records') AS apr(record)
        WHERE result.item->>'business_unit_id' = c_single_apr_business_unit_id::text
          AND apr.record->>'child_account_or_apr' = c_single_apr_text;

        ASSERT v_single_apr->>'accounting_division' = '99809902', 'BU 9902 accounting_division is incorrect';
        ASSERT v_single_apr->>'parent_account' = c_single_apr_account_number, 'Single APR parent account is incorrect';
        ASSERT v_single_apr->>'insert_date' = '26033', 'Single APR insert_date should use YYDDD';
        ASSERT v_single_apr->>'update_date' = '26034', 'Single APR update_date should use YYDDD';

        RAISE NOTICE 'TEST 3 PASSED';
    EXCEPTION
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 3 FAILED [%]: %', v_error_state, v_error_message;
    END;

    RAISE NOTICE '=== TEST 4: Interface-job rows and zero-balance exclusion ===';
    BEGIN
        SELECT count(*)
        INTO v_count
        FROM interface_jobs jobs
        WHERE jobs.business_unit_id IN (c_first_business_unit_id, c_single_apr_business_unit_id)
          AND jobs.interface_name = 'ant_csv'
          AND jobs.status = 'PROCESSING'
          AND jobs.created_datetime IS NOT NULL
          AND jobs.started_datetime IS NOT NULL
          AND jobs.completed_datetime IS NULL;
        ASSERT v_count = 2,
            format('Expected two PROCESSING interface-job rows; found %s', v_count);

        SELECT count(*)
        INTO v_count
        FROM interface_jobs jobs
        WHERE jobs.business_unit_id = c_zero_business_unit_id
          AND jobs.interface_name = 'ant_csv';
        ASSERT v_count = 0,
            'A BU with zero balance created an interface_jobs row';

        ASSERT NOT EXISTS (
            SELECT 1
            FROM unnest(v_procedure_output) AS result(item)
            WHERE result.item->>'interface_job_id' IS NULL
               OR NOT EXISTS (
                   SELECT 1
                   FROM interface_jobs jobs
                   WHERE jobs.interface_job_id = (result.item->>'interface_job_id')::bigint
                     AND jobs.business_unit_id = (result.item->>'business_unit_id')::smallint
                     AND jobs.interface_name = 'ant_csv'
               )
        ), 'A returned interface_job_id does not match its interface_jobs row';

        RAISE NOTICE 'TEST 4 PASSED';
    EXCEPTION
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 4 FAILED [%]: %', v_error_state, v_error_message;
    END;

    RAISE NOTICE '=== TEST 5: APR records are ordered by alternate_payment_reference_id ===';
    BEGIN
        SELECT min(apr.ordinality) FILTER (WHERE apr.record->>'child_account_or_apr' = c_first_apr_text),
               min(apr.ordinality) FILTER (WHERE apr.record->>'child_account_or_apr' = c_second_apr_text)
        INTO v_first_apr_ordinal, v_second_apr_ordinal
        FROM unnest(v_procedure_output) AS result(item)
        CROSS JOIN LATERAL json_array_elements(result.item->'apr_records') WITH ORDINALITY AS apr(record, ordinality)
        WHERE result.item->>'business_unit_id' = c_first_business_unit_id::text;
        ASSERT v_first_apr_ordinal IS NOT NULL
           AND v_second_apr_ordinal IS NOT NULL
           AND v_first_apr_ordinal < v_second_apr_ordinal,
            'APR records are not ordered by alternate_payment_reference_id';

        RAISE NOTICE 'TEST 5 PASSED';
    EXCEPTION
        WHEN ASSERT_FAILURE OR OTHERS THEN
            GET STACKED DIAGNOSTICS
                v_error_state = RETURNED_SQLSTATE,
                v_error_message = MESSAGE_TEXT;
            v_failed_test_count := v_failed_test_count + 1;
            RAISE WARNING 'TEST 5 FAILED [%]: %', v_error_state, v_error_message;
    END;

    IF v_failed_test_count > 0 THEN
        RAISE EXCEPTION '% p_int_ant_csv_extract test(s) failed; see warnings above', v_failed_test_count;
    END IF;

    RAISE NOTICE 'All p_int_ant_csv_extract unit tests passed';
END;
$$;

ROLLBACK;
