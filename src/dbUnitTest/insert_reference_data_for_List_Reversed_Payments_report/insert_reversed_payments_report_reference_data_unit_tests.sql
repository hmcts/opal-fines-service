
DO $$
DECLARE
    v_report reports%ROWTYPE;
    v_record_count INTEGER;
BEGIN
    RAISE NOTICE '=== PO-10345: Verify Reversed Payments Report reference data ===';

    SELECT COUNT(*)
    INTO v_record_count
    FROM reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT v_record_count = 1,
        'Expected exactly one Reversed Payments Report record';

    SELECT *
    INTO STRICT v_report
    FROM reports
    WHERE report_id = 'reversed_payments_report';

    ASSERT v_report.report_title = 'Reversed Payments Report',
        'Incorrect report title';

    ASSERT v_report.report_group = 'Cash reports',
        'Incorrect report group';

    ASSERT v_report.report_parameters IS NULL,
        'Report parameters should be NULL';

    ASSERT v_report.audited_report IS FALSE,
        'Audited report should be FALSE';

    ASSERT v_report.supports_multi_bu IS FALSE,
        'Supports multi BU should be FALSE';

    ASSERT v_report.is_bespoke_journey IS FALSE,
        'Bespoke journey should be FALSE';

    ASSERT v_report.shown_as_worklist IS FALSE,
        'Shown as worklist should be FALSE';

    ASSERT v_report.retention_period IS NULL,
        'Retention period should be NULL';

    ASSERT v_report.permission = 'REVERSE_PAYMENT_REPORT',
        'Incorrect report permission';

    ASSERT v_report.supported_file_types =
        ARRAY['PDF']::r_supported_file_type_enum[],
        'Supported file types should contain PDF only';

    ASSERT v_report.can_manually_create IS TRUE,
        'Can manually create should be TRUE';

    RAISE NOTICE 'PASS: PO-10345 - All reference data validated successfully';
END $$;
