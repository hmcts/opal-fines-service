/**
* CGI OPAL Program
*
* MODULE      : fix_dev_test_data_last_enforcement_links.sql
*
* DESCRIPTION : Correct dev seed enforcement data so seeded defendant accounts use valid
*               RESULTS.RESULT_ID values for LAST_ENFORCEMENT and matching ENFORCEMENTS rows.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------------------------------------------
* 08/05/2026    C Cho       1.0         PO-3798 DB- Inconsistent enforcement-related dev seed data across seeded defendant accounts
*
**/

 --Fix defendants accounts that currently have a non-null last_enforcement but inconsistent enforcement linkage. 
 --by replacing invented codes with valid results.result_id values
WITH seeded_account_enforcement_fix AS (
    SELECT *
      FROM (
            VALUES
                (99000000000001::bigint, 'FSN'::varchar(6)),
                (99000000000002::bigint, 'REM'::varchar(6)),
                (99000000000004::bigint, 'SUMM'::varchar(6)),
                (99000000000005::bigint, 'FSN'::varchar(6)),
                (99000000000006::bigint, 'DW'::varchar(6)),
                (99000000000007::bigint, 'FSN'::varchar(6)),
                (99000000000009::bigint, 'REM'::varchar(6)),
                (99000000000013::bigint, 'FSN'::varchar(6)),
                (99000000000014::bigint, 'SUMM'::varchar(6)),
                (99000000000016::bigint, 'REM'::varchar(6)),
                (99000000000017::bigint, 'FSN'::varchar(6)),
                (99000000000018::bigint, 'SUMM'::varchar(6))
           ) seeded_accounts(defendant_account_id, result_id)
)
UPDATE defendant_accounts da
   SET last_enforcement = seeded_account_enforcement_fix.result_id
  FROM seeded_account_enforcement_fix
 WHERE da.defendant_account_id = seeded_account_enforcement_fix.defendant_account_id
   AND da.last_enforcement IS DISTINCT FROM seeded_account_enforcement_fix.result_id;

WITH seeded_account_enforcement_fix AS (
    SELECT *
      FROM (
            VALUES
                (99000000000001::bigint, 'FSN'::varchar(6)),
                (99000000000002::bigint, 'REM'::varchar(6)),
                (99000000000004::bigint, 'SUMM'::varchar(6)),
                (99000000000005::bigint, 'FSN'::varchar(6)),
                (99000000000006::bigint, 'DW'::varchar(6)),
                (99000000000007::bigint, 'FSN'::varchar(6)),
                (99000000000009::bigint, 'REM'::varchar(6)),
                (99000000000013::bigint, 'FSN'::varchar(6)),
                (99000000000014::bigint, 'SUMM'::varchar(6)),
                (99000000000016::bigint, 'REM'::varchar(6)),
                (99000000000017::bigint, 'FSN'::varchar(6)),
                (99000000000018::bigint, 'SUMM'::varchar(6))
           ) seeded_accounts(defendant_account_id, result_id)
)
UPDATE enforcements e
   SET result_id = seeded_account_enforcement_fix.result_id,
       enforcer_id = COALESCE(e.enforcer_id, da.enf_override_enforcer_id)
  FROM seeded_account_enforcement_fix
  JOIN defendant_accounts da
    ON da.defendant_account_id = seeded_account_enforcement_fix.defendant_account_id
 WHERE e.defendant_account_id = seeded_account_enforcement_fix.defendant_account_id
   AND (
           e.result_id IS DISTINCT FROM seeded_account_enforcement_fix.result_id
        OR (e.enforcer_id IS NULL AND da.enf_override_enforcer_id IS NOT NULL)
       );
