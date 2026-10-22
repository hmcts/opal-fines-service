INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code,
                            business_unit_type, welsh_language)
VALUES (10877, 'APR Integration Tests', 'APRT', 'Area', FALSE);

INSERT INTO defendant_accounts (defendant_account_id, business_unit_id, account_number,
                                amount_imposed, amount_paid,
                                account_balance, account_status, account_type, version_number)
VALUES (1087701, 10877, 'APR-MULTIPLE', 100.00, 0.00, 100.00, 'L', 'Fine', 0),
       (1087702, 10877, 'APR-EMPTY', 100.00, 0.00, 100.00, 'L', 'Fine', 0),
       (1087703, 10877, 'APR-OTHER', 100.00, 0.00, 100.00, 'L', 'Fine', 0);

INSERT INTO alternate_payment_references (alternate_payment_reference_id, defendant_account_id,
                                          relationship, category, business_unit_id,
                                          apr_text, created_datetime, updated_datetime,
                                          version_number)
VALUES (10877001, 1087701, 'APR', 'APR', 10877, 'APR-REFERENCE-1',
        '2026-10-20 09:15:30', '2026-10-21 10:20:40', 2),
       (10877002, 1087701, 'CONSOLIDATED', 'ACC', 10877, 'APR-REFERENCE-2',
        '2026-10-18 11:25:35', '2026-10-19 12:30:45', 4),
       (10877003, 1087703, 'AMALGAMATED', 'ACC', 10877, 'APR-OTHER-ACCOUNT',
        '2026-10-17 13:35:50', '2026-10-18 14:40:55', 1);
