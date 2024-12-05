CREATE OR REPLACE PROCEDURE p_update_cheque_status(
    IN pi_cheque_id cheques.cheque_id%TYPE,
    IN pi_status cheques.status%TYPE)
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
*
**/
DECLARE
BEGIN
    UPDATE  cheques
    SET     status = pi_status
    WHERE   cheque_id = pi_cheque_id;
END;
$$ LANGUAGE plpgsql;
