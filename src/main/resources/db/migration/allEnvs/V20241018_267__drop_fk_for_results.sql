/**
* OPAL Program
*
* MODULE      : drop_fk_for_results.sql
*
* DESCRIPTION : Drop the foreign key with data that refers to RESULTS table to enable a cleanup in order to be able to load reference data from Capita. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------
* 18/10/2024    A Dennis    1.0         PO-900 Drop the foreign key with data that refers to RESULTS table to enable a cleanup in order to be able to load reference data from Capita.
**/

ALTER TABLE defendant_accounts
DROP constraint IF EXISTS da_enf_override_result_id_fk;
