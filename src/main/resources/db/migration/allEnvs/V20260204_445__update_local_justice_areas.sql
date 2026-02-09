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

--Update FK column on defendant_accounts for related local_justice_areas records that are about to be deleted
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 401
 WHERE enf_override_tfo_lja_id = 101;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 402
 WHERE enf_override_tfo_lja_id = 102;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 403
 WHERE enf_override_tfo_lja_id = 103;
 
UPDATE defendant_accounts 
   SET enf_override_tfo_lja_id = 404
 WHERE enf_override_tfo_lja_id = 106;

DELETE FROM local_justice_areas WHERE lja_type IN ('YCT', 'CTYCRT');

UPDATE local_justice_areas 
   SET lja_type = 'LJA'
 WHERE lja_type = 'PSA'; 