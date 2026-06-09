/**
* OPAL Program
*
* MODULE      : add_posted_by_name_to_audit_finalise.sql
*
* DESCRIPTION : Add posted by name support to p_audit_finalise.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 09/06/2026    HMCTS       1.0         PO-2623 Add pi_posted_by_name to p_audit_finalise and store it on AMENDMENTS.AMENDED_BY_NAME.
*
**/

CREATE PROCEDURE public.p_audit_finalise(
    IN pi_associated_account_id bigint,
    IN pi_record_type character varying,
    IN pi_business_unit_id smallint,
    IN pi_posted_by character varying,
    IN pi_posted_by_name character varying,
    IN pi_case_reference character varying,
    IN pi_function_code character varying
)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_before_amendment_id bigint;
BEGIN
    SELECT COALESCE(MAX(amendment_id), 0)
    INTO v_before_amendment_id
    FROM amendments;

    CALL public.p_audit_finalise(
        pi_associated_account_id,
        pi_record_type,
        pi_business_unit_id,
        pi_posted_by,
        pi_case_reference,
        pi_function_code
    );

    UPDATE amendments
    SET amended_by_name = pi_posted_by_name
    WHERE amendment_id > v_before_amendment_id
      AND associated_record_type = pi_record_type
      AND associated_record_id = pi_associated_account_id::VARCHAR
      AND business_unit_id = pi_business_unit_id
      AND amended_by = pi_posted_by
      AND case_reference IS NOT DISTINCT FROM pi_case_reference
      AND function_code IS NOT DISTINCT FROM pi_function_code;
END;
$$;

COMMENT ON PROCEDURE public.p_audit_finalise(
    IN pi_associated_account_id bigint,
    IN pi_record_type character varying,
    IN pi_business_unit_id smallint,
    IN pi_posted_by character varying,
    IN pi_posted_by_name character varying,
    IN pi_case_reference character varying,
    IN pi_function_code character varying
) IS 'Procedure to fetch current values of auditable amendment data fields, compare with stored initial values, and record changes in the amendments table.';
