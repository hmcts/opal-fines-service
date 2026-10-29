/**
* OPAL Program
*
* MODULE      : update_report_instances_location.sql
*
* DESCRIPTION : Update location data in the report_instances table - Dev only.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 12/10/2026    TMc            1.0         PO-10589 - Alter report_instances table to add template name
*
**/

UPDATE report_instances
   SET location = '00000000-0000-0000-0000-000000000000'::UUID
 WHERE location IS NOT NULL;

