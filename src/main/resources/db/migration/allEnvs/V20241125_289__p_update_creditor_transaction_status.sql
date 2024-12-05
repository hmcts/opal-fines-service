CREATE OR REPLACE PROCEDURE p_update_creditor_transaction_status(
    IN pi_creditor_transaction_id creditor_transactions.creditor_transaction_id%TYPE,
    IN pi_status creditor_transactions.status%TYPE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_update_creditor_transaction_status.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
BEGIN
    UPDATE  creditor_transactions
    SET     status = pi_status,
            status_date = CURRENT_TIMESTAMP
    WHERE   creditor_transaction_id = pi_creditor_transaction_id;
END;
$$ LANGUAGE plpgsql;
