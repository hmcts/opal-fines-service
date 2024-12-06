/**
* OPAL Program
*
* MODULE      : cleanup_results.sql
*
* DESCRIPTION : Drop the RESULTS table to enable a cleanup in order to be able to load reference data from Capita. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------
* 05/09/2024    A Dennis    1.0         PO-701 Drop the RESULTS table to enable a cleanup in order to be able to load reference data from Capita.  
*
**/

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS res_enf_override_result_id_fk;

ALTER TABLE enforcements
DROP constraint IF EXISTS enf_result_id_fk;

ALTER TABLE committal_warrant_progress
DROP constraint IF EXISTS enf_enforcement_id_fk;

DELETE FROM committal_warrant_progress;

DELETE FROM enforcements;

ALTER TABLE impositions 
DROP constraint IF EXISTS imp_result_id_fk;

ALTER TABLE result_documents
DROP constraint IF EXISTS rd_result_id_fk;

DROP TABLE results;
