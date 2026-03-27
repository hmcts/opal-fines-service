/**
* CGI OPAL Program
*
* MODULE      : amend_view_v_search_defendant_accounts_consolidation.sql
*
* DESCRIPTION : Create view to retrieve defendant account information for consolidation search
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------
* 15/12/2025    C Cho       1.0         PO-2304 - Create consolidation search view
* 06/03/2026    T McCallion 2.0         Need to recreate view as part of new ENUMs work
*                                       Taken from: V20251217_435__create_view_v_search_defendant_accounts_consolidation.sql
*                                                   Removed DDL for function has_uncleared_cheques and added DROP statement.
*                                                   Invalid value (TFOA) has been removed from both CASE statements on vsda.account_status.
*
**/
DROP VIEW IF EXISTS v_search_defendant_accounts_consolidation;

-- Main consolidation view
CREATE OR REPLACE VIEW v_search_defendant_accounts_consolidation AS
SELECT 
    vsda.*,
    da.collection_order AS has_collection_order,
    da.version_number,
    
    -- Error messages
    CASE 
        WHEN (
            -- Count all error conditions
            (CASE WHEN vsda.account_status IN ('TA', 'WO', 'CS') THEN 1 ELSE 0 END) +
            (CASE WHEN vsda.last_enforcement IN ('BWTD', 'BWTU', 'CLAMPO', 'CONF', 'CW', 'CWN', 'DW', 'NAP', 'NAWT', 'NBWT', 'REW', 'S136', 'S18', 'SC', 'SUMM', 'TFOOUT') THEN 1 ELSE 0 END) +
            (CASE WHEN COALESCE(da.jail_days, 0) > 0 THEN 1 ELSE 0 END) +
            (CASE WHEN da.enforcement_case_status IS NOT NULL THEN 1 ELSE 0 END)
        ) = 0 THEN NULL
        ELSE 
            ARRAY_REMOVE(
                ARRAY[
                    CASE WHEN vsda.account_status IN ('TA', 'WO', 'CS') 
                        THEN 'CON.ER.1|Account status is `' || vsda.account_status || '`' END,
                    CASE WHEN vsda.last_enforcement IN ('BWTD', 'BWTU', 'CLAMPO', 'CONF', 'CW', 'CWN', 'DW', 'NAP', 'NAWT', 'NBWT', 'REW', 'S136', 'S18', 'SC', 'SUMM', 'TFOOUT')
                        THEN 'CON.ER.3|Last enforcement action on the account is `' || 
                            COALESCE(r_last.result_title || '(' || vsda.last_enforcement || ')', vsda.last_enforcement) || '`' END,
                    CASE WHEN COALESCE(da.jail_days, 0) > 0 
                        THEN 'CON.ER.4|Account has days in default' END,
                    CASE WHEN da.enforcement_case_status IS NOT NULL 
                        THEN 'CON.ER.5|Account linked to outstanding active case' END
                ], 
                NULL
            ) END AS errors,
    
    -- Warning messages
    CASE 
        WHEN (
            -- Count all warning conditions
            (CASE WHEN vsda.last_enforcement IN ('ABDC', 'AEO', 'AEOC', 'FSN', 'MAN', 'MPSO', 'REGF', 'UPWO', 'PRIS', 'NOENF') THEN 1 ELSE 0 END) +
            (CASE WHEN has_uncleared_cheques(da.defendant_account_id) = TRUE THEN 1 ELSE 0 END)
        ) = 0 THEN NULL
        ELSE 
            ARRAY_REMOVE(
                ARRAY[
                    CASE WHEN vsda.last_enforcement IN ('ABDC', 'AEO', 'AEOC', 'FSN', 'MAN', 'MPSO', 'REGF', 'UPWO', 'PRIS', 'NOENF')
                        THEN 'CON.WN.1|Last enforcement action on the account is `' || 
                            COALESCE(r_last.result_title || '(' || vsda.last_enforcement || ')', vsda.last_enforcement) || '`' END,
                    CASE WHEN has_uncleared_cheques(da.defendant_account_id) = TRUE 
                        THEN 'CON.WN.2|Account has uncleared cheque payments' END
                ],
                NULL
            ) END AS warnings

FROM defendant_accounts da
JOIN v_search_defendant_accounts vsda
    ON da.defendant_account_id = vsda.defendant_account_id
LEFT JOIN results r_last
    ON r_last.result_id = vsda.last_enforcement;

COMMENT ON VIEW v_search_defendant_accounts_consolidation IS 'Retrieves defendant account information for consolidation search';