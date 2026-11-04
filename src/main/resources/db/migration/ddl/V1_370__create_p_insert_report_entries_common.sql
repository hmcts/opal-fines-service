
/**
 * OPAL Program
 *
 * MODULE      : V1_1__create_p_insert_report_entries_common.sql
 *
 * DESCRIPTION : Creates the common procedure for inserting
 *               records into REPORT_ENTRIES.
 *
 * VERSION HISTORY:
 *
 * Date         Author       Version   Nature of Change
 * -----------  -----------  --------  -------------------------
 * 03/11/2026   I Chandio    0.1       Initial version Create p_insert_report_entries_common to insert data into the report_entries table
 */

CREATE OR REPLACE PROCEDURE public.p_insert_report_entries_common(
    IN pi_business_unit_id SMALLINT,
    IN pi_report_id VARCHAR,
    IN pi_entry_timestamp TIMESTAMP WITHOUT TIME ZONE,
    IN pi_reported_timestamp TIMESTAMP WITHOUT TIME ZONE,
    IN pi_associated_record_type public.t_associated_record_type_enum,
    IN pi_associated_record_id VARCHAR,
    IN pi_report_instance_id BIGINT,
    OUT po_report_entry_id BIGINT
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_pg_exception_detail TEXT;

BEGIN

    INSERT INTO public.report_entries (
        report_entry_id,
        business_unit_id,
        report_id,
        entry_timestamp,
        reported_timestamp,
        associated_record_type,
        associated_record_id,
        report_instance_id
    )
    VALUES (
        nextval('public.report_entry_id_seq'),
        pi_business_unit_id,
        pi_report_id,
        pi_entry_timestamp,
        pi_reported_timestamp,
        pi_associated_record_type,
        pi_associated_record_id,
        pi_report_instance_id
    )
    RETURNING report_entry_id
    INTO po_report_entry_id;

EXCEPTION

    WHEN not_null_violation
      OR foreign_key_violation
      OR unique_violation
      OR check_violation
    THEN
        RAISE NOTICE
            'Error in p_insert_report_entries_common: % - %',
            SQLSTATE, SQLERRM;
        RAISE;

    WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS
            v_pg_exception_detail = PG_EXCEPTION_DETAIL;

        RAISE NOTICE
            'Error in p_insert_report_entries_common: % - %',
            SQLSTATE, SQLERRM;

        RAISE NOTICE
            'Error details: %',
            v_pg_exception_detail;

        RAISE;

END;
$procedure$;


COMMENT ON PROCEDURE public.p_insert_report_entries_common(
    SMALLINT,
    VARCHAR,
    TIMESTAMP WITHOUT TIME ZONE,
    TIMESTAMP WITHOUT TIME ZONE,
    public.t_associated_record_type_enum,
    VARCHAR,
    BIGINT
)
IS 'Insert a report entry into REPORT_ENTRIES and return the generated report_entry_id.';
