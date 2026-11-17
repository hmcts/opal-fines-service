CREATE OR REPLACE PROCEDURE p_int_ant_csv_extract(
    OUT po_apr_details_json JSON[]
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
* PARAMETERS  : po_apr_details_json - JSON array containing interface job IDs, business unit IDs, and APR records
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
BEGIN
    -- Identify each BU with at least one APR linked to a non-zero-balance account.
    -- Keep this eligibility set separate from JSON construction so every qualifying BU gets a job.
    WITH eligible_business_units AS (
        SELECT DISTINCT apr.business_unit_id
        FROM alternate_payment_references apr
        JOIN defendant_accounts da
          ON da.defendant_account_id = apr.defendant_account_id
        WHERE da.account_balance <> 0
    ),
    -- Aggregate the eligible APR rows for each BU in APR ID order, using the required ANT CSV labels.
    aggregated_apr AS (
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
                       'date_close_status', to_char(da.completed_date, 'YYDDD'),
                       'insert_date', to_char(apr.created_datetime, 'YYDDD'),
                       'update_date', to_char(apr.updated_datetime, 'YYDDD'),
                       'status', 'A',
                       'relationship', left(apr.relationship::TEXT, 3)
                   )
                   ORDER BY apr.alternate_payment_reference_id
               ) AS apr_records
        FROM alternate_payment_references apr
        JOIN defendant_accounts da
          ON da.defendant_account_id = apr.defendant_account_id
        JOIN business_units ad_bu
          ON ad_bu.business_unit_id = apr.business_unit_id
        JOIN business_units area_bu
          ON area_bu.business_unit_id = ad_bu.parent_business_unit_id
         AND area_bu.business_unit_type = 'Area'
        WHERE da.account_balance <> 0
        GROUP BY apr.business_unit_id
    ),
    -- If no matching APR rows, return an empty APR array for that BU.
    business_unit_results AS (
        SELECT eligible.business_unit_id,
               COALESCE(aggregated.apr_records, '[]'::JSON) AS apr_records
        FROM eligible_business_units eligible
        LEFT JOIN aggregated_apr aggregated
          ON aggregated.business_unit_id = eligible.business_unit_id
    ),
    -- Insert one interface job per qualifying BU and keep its generated ID for the returned object.
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
        SELECT nextval('interface_job_id_seq'),
               results.business_unit_id,
               'ant_csv',
               'PROCESSING',
               CURRENT_TIMESTAMP,
               CURRENT_TIMESTAMP,
               NULL
        FROM business_unit_results results
        RETURNING interface_job_id, business_unit_id
    )
    -- put together the procedure's JSON[] output in business-unit order.
    SELECT COALESCE(
               array_agg(
                   json_build_object(
                       'interface_job_id', jobs.interface_job_id,
                       'business_unit_id', results.business_unit_id,
                       'apr_records', results.apr_records
                   )
                   ORDER BY results.business_unit_id
               ),
               ARRAY[]::JSON[]
           )
    INTO po_apr_details_json
    FROM business_unit_results results
    JOIN inserted_jobs jobs
      ON jobs.business_unit_id = results.business_unit_id;
EXCEPTION
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_int_ant_csv_extract: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_int_ant_csv_extract: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$$;
