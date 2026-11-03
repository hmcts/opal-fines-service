/**
* OPAL Program
*
* MODULE      : compare_legacy_table_with_local_justice_areas.sql
*
* DESCRIPTION : Compare data in the LOCAL_JUSTICE_AREAS_LEGACY table with the data in the LOCAL_JUSTICE_AREAS table.
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
    FROM local_justice_areas AS o
    EXCEPT
    SELECT 'Original' AS source, l.*
    FROM local_justice_areas_legacy AS l
)
UNION ALL
(
    SELECT 'Legacy' AS source, l.*
    FROM local_justice_areas_legacy AS l
    EXCEPT
    SELECT 'Legacy' AS source, o.*
    FROM local_justice_areas AS o
)
ORDER BY local_justice_area_id, source;