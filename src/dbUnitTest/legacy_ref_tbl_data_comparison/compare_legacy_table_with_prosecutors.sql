/**
* OPAL Program
*
* MODULE      : compare_legacy_table_with_prosecutors.sql
*
* DESCRIPTION : Compare data in the PROSECUTORS_LEGACY table with the data in the PROSECUTORS table.
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
    FROM prosecutors AS o
    EXCEPT
    SELECT 'Original' AS source, l.*
    FROM prosecutors_legacy AS l
)
UNION ALL
(
    SELECT 'Legacy' AS source, l.*
    FROM prosecutors_legacy AS l
    EXCEPT
    SELECT 'Legacy' AS source, o.*
    FROM prosecutors AS o
)
ORDER BY prosecutor_id, source;