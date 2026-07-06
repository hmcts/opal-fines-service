/**
* OPAL Program
*
* MODULE      : alter_p_insert_till_add_interface_metadata.sql
*
* DESCRIPTION : Amend stored procedure p_insert_till for Auto Payments In Processing Files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 17/06/2026    C Cho       1.0         PO-2617 Amend stored procedure p_insert_till for Auto Payments In Processing Files
*
**/

DROP PROCEDURE IF EXISTS public.p_insert_till(bigint, smallint, smallint);

CREATE PROCEDURE public.p_insert_till(
    INOUT pio_till_id tills.till_id%TYPE,
    INOUT pio_till_number tills.till_number%TYPE,
    IN pi_business_unit_id tills.business_unit_id%TYPE,
    IN pi_source tills.source%TYPE,
    IN pi_interface_file_id interface_files.interface_file_id%TYPE,
    IN pi_posted_by tills.owned_by%TYPE,
    IN pi_posted_by_name tills.owned_by_name%TYPE
)
    LANGUAGE plpgsql
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
* 17/06/2026    C Cho       2.0         PO-2617 Amend stored procedure p_insert_till for Auto Payments IN Processing Files
*
**/
BEGIN
    INSERT INTO tills (
                    till_id,
                    business_unit_id,
                    till_number,
                    owned_by,
                    owned_by_name,
                    status,
                    source,
                    interface_file_id,
                    auto_payment,
                    created_date)
    VALUES      (
                    nextval('till_id_seq'),
                    pi_business_unit_id,
                    nextval('till_number_'||pi_business_unit_id::text||'_seq'),
                    pi_posted_by,
                    pi_posted_by_name,
                    'Created',
                    pi_source,
                    pi_interface_file_id,
                    TRUE,
                    CURRENT_TIMESTAMP)
    RETURNING   till_id, till_number
    INTO        pio_till_id, pio_till_number;
END;
$$;
