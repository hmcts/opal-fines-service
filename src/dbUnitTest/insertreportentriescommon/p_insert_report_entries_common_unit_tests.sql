-- PO-10348: Execute after applying the migration.
-- Run using psql -X -v ON_ERROR_STOP=1 -f <this_file>.
-- Test rows are rolled back. Sequence values are NOT rolled back by PostgreSQL.
BEGIN;
DO $tests$
DECLARE
    v_business_unit_id public.report_entries.business_unit_id%TYPE;
    v_report_id public.report_entries.report_id%TYPE;
    v_first_id public.report_entries.report_entry_id%TYPE;
    v_second_id public.report_entries.report_entry_id%TYPE;
    v_expected_ts timestamp := timestamp '2026-11-02 15:30:00';
    v_test_ref text := 'PO-10348-UNIT-TEST';
    v_count integer;
    v_sqlstate text;
BEGIN
    -- TEST 1: Procedure exists. This also distinguishes a procedure from a function.
    SELECT count(*) INTO v_count
    FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND p.proname = 'p_insert_report_entries_common'
      AND p.prokind = 'p';
    IF v_count <> 1 THEN
        RAISE EXCEPTION 'TEST 1 FAILED: Expected exactly one procedure, found %', v_count;
    END IF;
    RAISE NOTICE 'TEST 1 PASSED: procedure exists';

    -- Select a real business unit/report to satisfy foreign keys. No test-data inserts.
    SELECT business_unit_id INTO v_business_unit_id
    FROM public.business_units ORDER BY business_unit_id LIMIT 1;
    SELECT report_id INTO v_report_id
    FROM public.reports ORDER BY report_id LIMIT 1;
    IF v_business_unit_id IS NULL OR v_report_id IS NULL THEN
        RAISE EXCEPTION 'FIXTURE ERROR: business_units and reports must each contain at least one row';
    END IF;

    -- TEST 2: Insert values, return ID, preserve NULL optional values.
    CALL public.p_insert_report_entries_common(
        v_business_unit_id, v_report_id, v_expected_ts, NULL,
        'suspense_items'::public.t_associated_record_type_enum,
        v_test_ref, NULL, v_first_id
    );
    IF v_first_id IS NULL THEN
        RAISE EXCEPTION 'TEST 2 FAILED: OUT ID is NULL';
    END IF;
    SELECT count(*) INTO v_count
    FROM public.report_entries
    WHERE report_entry_id = v_first_id
      AND business_unit_id = v_business_unit_id
      AND report_id = v_report_id
      AND entry_timestamp = v_expected_ts
      AND reported_timestamp IS NULL
      AND associated_record_type = 'suspense_items'::public.t_associated_record_type_enum
      AND associated_record_id = v_test_ref
      AND report_instance_id IS NULL;
    IF v_count <> 1 THEN
        RAISE EXCEPTION 'TEST 2 FAILED: Inserted row/column values do not match';
    END IF;
    RAISE NOTICE 'TEST 2 PASSED: row inserted with correct values and returned ID %', v_first_id;

    -- TEST 3: A second call generates a distinct ID and preserves a non-NULL reported timestamp.
    CALL public.p_insert_report_entries_common(
        v_business_unit_id, v_report_id, v_expected_ts, v_expected_ts,
        'suspense_items'::public.t_associated_record_type_enum,
        v_test_ref || '-2', NULL, v_second_id
    );
    IF v_second_id IS NULL OR v_second_id = v_first_id THEN
        RAISE EXCEPTION 'TEST 3 FAILED: Second ID is missing/duplicated';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM public.report_entries
        WHERE report_entry_id = v_second_id
          AND reported_timestamp = v_expected_ts
          AND associated_record_id = v_test_ref || '-2'
    ) THEN
        RAISE EXCEPTION 'TEST 3 FAILED: Second row not persisted correctly';
    END IF;
    RAISE NOTICE 'TEST 3 PASSED: second insert generated distinct ID %', v_second_id;

    -- TEST 4: A NOT NULL violation is propagated without being swallowed or rewritten.
    v_sqlstate := NULL;
    BEGIN
        CALL public.p_insert_report_entries_common(
            v_business_unit_id, NULL, v_expected_ts, NULL,
            'suspense_items'::public.t_associated_record_type_enum,
            v_test_ref || '-INVALID', NULL, v_second_id
        );
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
    END;
    IF v_sqlstate IS DISTINCT FROM '23502' THEN
        RAISE EXCEPTION 'TEST 4 FAILED: Expected SQLSTATE 23502, got %', v_sqlstate;
    END IF;
    RAISE NOTICE 'TEST 4 PASSED: NOT NULL error propagated (23502)';

    -- TEST 5: Bad report FK propagates the database SQLSTATE 23503.
    v_sqlstate := NULL;
    BEGIN
        CALL public.p_insert_report_entries_common(
            v_business_unit_id, '__PO_10348_INVALID_REPORT__', v_expected_ts, NULL,
            'suspense_items'::public.t_associated_record_type_enum,
            v_test_ref || '-INVALID-FK', NULL, v_second_id
        );
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_sqlstate = RETURNED_SQLSTATE;
    END;
    IF v_sqlstate IS DISTINCT FROM '23503' THEN
        RAISE EXCEPTION 'TEST 5 FAILED: Expected SQLSTATE 23503, got %', v_sqlstate;
    END IF;
    RAISE NOTICE 'TEST 5 PASSED: FK violation propagated (23503)';

    RAISE NOTICE 'ALL PO-10348 TESTS PASSED';
END;
$tests$;
ROLLBACK;
