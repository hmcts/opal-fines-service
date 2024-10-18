/**
* OPAL Program
*
* MODULE      : restore_fk_after_results.sql
*
* DESCRIPTION : Update the enf_override_result_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new result_ids in the RESULTS table after reference data load. Enable foreign keys that were disabled to allow Results reference data load.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 18/10/2024    A Dennis    1.0         PO-900  Update the enf_override_result_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new result_ids in the RESULTS table after reference data load. Enable foreign keys that were disabled to allow Results reference data load. 
*
**/

ALTER TABLE defendant_accounts
ADD CONSTRAINT da_enf_override_result_id_fk FOREIGN KEY
(
  enf_override_result_id
)
REFERENCES results
(
  result_id 
);
