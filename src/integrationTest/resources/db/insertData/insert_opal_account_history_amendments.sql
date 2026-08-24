INSERT INTO amendments (
    amendment_id, business_unit_id, associated_record_type, associated_record_id, amended_date,
    amended_by, field_code, old_value, new_value, case_reference, function_code
) VALUES
(
    26220006, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-06 08:00:00',
    'hist-user-6', 2, 'Old two', 'New two', 'CASE-HIST-2', 'UPD'
),
(
    26220007, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-07 08:00:00',
    'hist-user-7', 1, 'Old three', 'New three', 'CASE-HIST-3', 'UPD'
);
