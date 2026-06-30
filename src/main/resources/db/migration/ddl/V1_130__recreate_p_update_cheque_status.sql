--Update signature for p_update_cheque_status
DROP PROCEDURE IF EXISTS p_update_cheque_status;

CREATE OR REPLACE PROCEDURE p_update_cheque_status(
    IN pi_cheque_id cheques.cheque_id%TYPE,
    IN pi_status cheques.status%TYPE)
LANGUAGE 'plpgsql'
AS $$
/**
* OPAL Program
*
* MODULE      : p_update_cheque_status.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
* 03/06/2025    TMc         2.0         PO-3621 - Update columns on CHEQUES table to use PostgreSQL ENUM
*                                       Dropped and recreated SP as signature has changed. No other changes made.
*
**/
DECLARE
BEGIN
    UPDATE  cheques
    SET     status = pi_status
    WHERE   cheque_id = pi_cheque_id;
END;
$$;
