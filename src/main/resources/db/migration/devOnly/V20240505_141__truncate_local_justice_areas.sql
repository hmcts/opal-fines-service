/**
* OPAL Program
*
* MODULE      : modify_local_justice_areas.sql
*
* DESCRIPTION : Truncate the LOCAL_JUSTICE_AREAS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------
* 05/05/2024    A Dennis    1.0         PO-305 Truncate the LOCAL_JUSTICE_AREAS table in order to be able to load Reference Data from data held in Excel spreadsheet. 
*
**/

TRUNCATE TABLE local_justice_areas;
