/**
* OPAL Program
*
* MODULE      : insert_missing_cf_creditor_accounts_nle_data.sql
*
* DESCRIPTION : Insert missing central fund creditor account records for accounting division business units.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 24/07/2026    C Cho       1.0         PO-5743 Insert missing CF creditor account records for NLE data.
*
**/

WITH missing_cf_recs AS (
    SELECT bu.business_unit_id
    FROM business_units bu
    WHERE NOT EXISTS (
        SELECT 1
        FROM creditor_accounts ca
        WHERE ca.creditor_account_type = 'CF'
          AND ca.business_unit_id = bu.business_unit_id
    )
      AND bu.business_unit_type = 'Accounting Division'
)
INSERT INTO creditor_accounts (
    creditor_account_id,
    business_unit_id,
    account_number,
    creditor_account_type,
    prosecution_service,
    major_creditor_id,
    minor_creditor_party_id,
    repayment,
    hold_payout,
    pay_by_bacs,
    bank_sort_code,
    bank_account_number,
    bank_account_name,
    bank_account_reference,
    bank_account_type,
    last_changed_date,
    version_number
)
SELECT
    11300000000198 + ROW_NUMBER() OVER (ORDER BY cte.business_unit_id),
    cte.business_unit_id,
    '00002000J',
    'CF'::public.t_creditor_account_type_enum,
    false,
    NULL,
    NULL,
    false,
    false,
    false,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    1
FROM missing_cf_recs cte;
