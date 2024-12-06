CREATE OR REPLACE FUNCTION f_get_master_account_id(
    IN pi_defendant_account_id defendant_accounts.defendant_account_id%TYPE)
    RETURNS defendant_accounts.defendant_account_id%TYPE
AS $$
/**
* OPAL Program
*
* MODULE      : f_get_master_account_id.sql
*
* DESCRIPTION : This function was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This function was written by Capita required for interface files
*
**/
DECLARE
    v_master_account_id defendant_accounts.defendant_account_id%TYPE;
BEGIN
    WITH RECURSIVE master_accounts AS (
        SELECT      da.defendant_account_id, dt.associated_record_id::bigint AS master_account_id
        FROM        defendant_accounts da
        LEFT JOIN   defendant_transactions dt ON da.account_status = 'CS' AND dt.defendant_account_id = da.defendant_account_id AND dt.transaction_type = 'WRTOFF' AND dt.text LIKE 'CONSOLIDATED%'
        WHERE       da.defendant_account_id = pi_defendant_account_id
        UNION ALL
        SELECT      da1.defendant_account_id, dt1.associated_record_id::bigint AS master_account_id
        FROM        defendant_accounts da1
        LEFT JOIN   defendant_transactions dt1 ON da1.account_status = 'CS' AND dt1.defendant_account_id = da1.defendant_account_id AND dt1.transaction_type = 'WRTOFF' AND dt1.text LIKE 'CONSOLIDATED%'
        INNER JOIN  master_accounts ma ON da1.defendant_account_id = ma.master_account_id
    )
    SELECT  defendant_account_id
    INTO    v_master_account_id
    FROM    master_accounts WHERE master_account_id IS NULL;
    RETURN v_master_account_id;
END;
$$ LANGUAGE plpgsql;