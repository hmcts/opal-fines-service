CREATE OR REPLACE PROCEDURE p_insert_till(
    INOUT pio_till_id tills.till_id%TYPE,
    INOUT pio_till_number tills.till_number%TYPE,
    IN pi_business_unit_id tills.business_unit_id%TYPE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_till.sql
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
BEGIN
    INSERT INTO tills (
                    till_id,
                    business_unit_id,
                    till_number)
    VALUES      (
                    nextval('till_id_seq'),
                    pi_business_unit_id,
                    nextval('till_number_'||pi_business_unit_id::text||'_seq'))
    RETURNING   till_id, till_number
    INTO        pio_till_id, pio_till_number;
END;
$$ LANGUAGE plpgsql;
