CREATE OR REPLACE PROCEDURE p_int_ant_csv_extract(
    OUT po_apr_details_json JSON
)
LANGUAGE plpgsql
AS $$
/**
* OPAL Program
*
* MODULE      : p_int_ant_csv_extract
*
* DESCRIPTION : Return ANT CSV APR data grouped by business unit and create interface jobs
*
* PARAMETERS  : po_apr_details_json - JSON value containing an array of interface job IDs, business unit IDs, and APR records
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 13/11/2026    TT/TMc         1.0         PO-11448 - Create p_int_ant_csv_extract for EI8
*
**/
DECLARE
    v_pg_exception_detail TEXT;
    c_date_format CONSTANT TEXT := 'YYDDD';
    c_area_business_unit_type CONSTANT t_business_unit_type_enum := 'Area'::t_business_unit_type_enum;
    c_interface_name CONSTANT TEXT := 'ant_csv';
    c_job_status CONSTANT t_interface_job_status_enum := 'PROCESSING'::t_interface_job_status_enum;
    c_interface_job_id_sequence CONSTANT REGCLASS := 'interface_job_id_seq'::REGCLASS;
BEGIN
    -- Aggregate qualifying APR rows by BU and build their ANT CSV records.
    -- Materialize this result so the job insert and returned JSON reuse the same grouped rows.
    WITH aggregated_apr AS MATERIALIZED (
        SELECT apr.business_unit_id,
               json_agg(
                   json_build_object(
                       'accounting_division',
                           lpad(area_bu.business_unit_code, 4, '0')
                           || lpad(ad_bu.business_unit_id::TEXT, 4, '0'),
                       'child_account_or_apr', apr.apr_text,
                       'category', apr.category,
                       'parent_account', da.account_number,
                       'parent_category', 'ACC',
                       'closed_status', CASE WHEN da.account_balance <> 0 THEN 'N' ELSE 'Y' END,
                       'date_close_status', to_char(da.completed_date, c_date_format),
                       'insert_date', to_char(apr.created_datetime, c_date_format),
                       'update_date', to_char(apr.updated_datetime, c_date_format),
                       'status', 'A',
                       'relationship', left(apr.relationship::TEXT, 3)
                   )
                   ORDER BY da.account_number, apr.alternate_payment_reference_id
               ) AS apr_records
        FROM alternate_payment_references apr
        JOIN defendant_accounts da
          ON da.defendant_account_id = apr.defendant_account_id
        JOIN business_units ad_bu
          ON ad_bu.business_unit_id = apr.business_unit_id
        JOIN business_units area_bu
          ON area_bu.business_unit_id = ad_bu.parent_business_unit_id
         AND area_bu.business_unit_type = c_area_business_unit_type
        WHERE da.account_balance <> 0
        GROUP BY apr.business_unit_id
    ),
    -- Insert one interface job per BU with qualifying APR data.
    inserted_jobs AS (
        INSERT INTO interface_jobs (
            interface_job_id,
            business_unit_id,
            interface_name,
            status,
            created_datetime,
            started_datetime,
            completed_datetime
        )
        SELECT nextval(c_interface_job_id_sequence),
               apr.business_unit_id,
               c_interface_name,
               c_job_status,
               CURRENT_TIMESTAMP,
               CURRENT_TIMESTAMP,
               NULL
        FROM aggregated_apr apr
        RETURNING interface_job_id, business_unit_id
    )
    -- Build one output object per BU, ordered by BU ID.
    SELECT COALESCE(
               json_agg(
                   json_build_object(
                       'interface_job_id', jobs.interface_job_id,
                       'business_unit_id', apr.business_unit_id,
                       'apr_records', apr.apr_records
                   )
                   ORDER BY apr.business_unit_id
               ),
               '[]'::JSON
           )
    INTO po_apr_details_json
    FROM aggregated_apr apr
    JOIN inserted_jobs jobs
      ON jobs.business_unit_id = apr.business_unit_id;
EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_int_ant_csv_extract: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_int_ant_csv_extract: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$$;
