/**
* CGI OPAL Program
*
* MODULE      : update_local_justice_areas.sql
*
* DESCRIPTION : Amend reference data in the LOCAL_JUSTICE_AREAS table
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 04/02/2026    T McCallion    1.0         PO-2752 - DB - Amend reference data in the LOCAL_JUSTICE_AREAS table
*
**/

DELETE FROM local_justice_areas WHERE lja_type IN ('YCT', 'CTYCRT');

UPDATE local_justice_areas 
   SET lja_type = 'LJA'
 WHERE lja_type = 'PSA'; 