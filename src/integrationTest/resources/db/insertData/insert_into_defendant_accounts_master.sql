-- Master account
INSERT INTO defendant_accounts (
    defendant_account_id,
    business_unit_id,
    account_number,
    amount_imposed,
    amount_paid,
    account_balance,
    account_status,
    account_type,
    version_number
) VALUES (
    990001,
    78,
    '990001M',
    100.00,
    0.00,
    100.00,
    'L'::t_da_account_status_enum,
    'Fine'::t_da_account_type_enum,
    1
),
-- Consolidated child account
(
    990002,
    78,
    '990002C',
    100.00,
    0.00,
    100.00,
    'CS'::t_da_account_status_enum,
    'Fine'::t_da_account_type_enum,
1
);

-- Links the child account to the master account
INSERT INTO defendant_transactions (
    defendant_transaction_id,
    defendant_account_id,
    posted_date,
    transaction_type,
    text,
    associated_record_type,
    associated_record_id
) VALUES (
    99000201,
    990002,
    CURRENT_TIMESTAMP,
    'WRTOFF'::t_defendant_transaction_type_enum,
    'CONSOLIDATED INTO MASTER ACCOUNT',
    'defendant_accounts'::t_associated_record_type_enum,
    '990001'
);