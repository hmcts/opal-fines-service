/**
* OPAL Program
*
* MODULE      : compare_legacy_table_with_offences.sql
*
* DESCRIPTION : Compare data in the OFFENCES_LEGACY table with the data in the OFFENCES table.
*               Expected Results: No records returned = Data in both tables are identical. 
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------------
* 19/10/2026    T McCallion    1.0         PO-10770 - Create _legacy R1A reference data tables
*
**/
(
    SELECT 'Original' AS source, o.*
    FROM offences AS o
    EXCEPT
    SELECT 'Original' AS source, l.*
    FROM offences_legacy AS l
)
UNION ALL
(
    SELECT 'Legacy' AS source, l.*
    FROM offences_legacy AS l
    EXCEPT
    SELECT 'Legacy' AS source, o.*
    FROM offences AS o
)
ORDER BY offence_id, source;