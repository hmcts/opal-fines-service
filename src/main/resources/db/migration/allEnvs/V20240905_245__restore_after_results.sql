/**
* OPAL Program
*
* MODULE      : restore_after_results.sql
*
* DESCRIPTION : Update the enf_override_result_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new result_ids in the RESULTS table after reference data load. Enable foreign keys that were disabled to allow Results reference data load.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/09/2024    A Dennis    1.0         PO-701  Update the enf_override_result_id foreign key ids in DEFENDANT_ACCOUNTS table to match the new result_ids in the RESULTS table after reference data load. Enable foreign keys that were disabled to allow Results reference data load. 
*
**/

UPDATE defendant_accounts
SET enf_override_result_id    = 'AEO'
WHERE enf_override_result_id  = '2723';

UPDATE defendant_accounts
SET enf_override_result_id    = 'BWTD'
WHERE enf_override_result_id  = '2607';

UPDATE defendant_accounts
SET enf_override_result_id    = 'BWTU'
WHERE enf_override_result_id  = '2631';

UPDATE defendant_accounts
SET enf_override_result_id    = 'CLAMPO'
WHERE enf_override_result_id  = '2679';

UPDATE defendant_accounts
SET enf_override_result_id    = 'FCOST'
WHERE enf_override_result_id  = '2223';

UPDATE defendant_accounts
SET enf_override_result_id    = 'FCOMP'
WHERE enf_override_result_id  = '2396';

UPDATE defendant_accounts
SET enf_override_result_id    = 'FEES'
WHERE enf_override_result_id  = '2406';

UPDATE defendant_accounts
SET enf_override_result_id    = 'FO'
WHERE enf_override_result_id  = '2646';

UPDATE defendant_accounts
SET enf_override_result_id    = 'SUMM'
WHERE enf_override_result_id  = '2409';

UPDATE defendant_accounts
SET enf_override_result_id    = 'UPWO'
WHERE enf_override_result_id  = '1838';

UPDATE defendant_accounts
SET enf_override_result_id    = 'FINE'
WHERE enf_override_result_id  = '1838';


ALTER TABLE defendant_accounts
ADD CONSTRAINT da_enf_override_result_id_fk FOREIGN KEY
(
  enf_override_result_id
)
REFERENCES results
(
  result_id 
);

ALTER TABLE defendant_accounts RENAME CONSTRAINT res_enf_override_enforcer_id_fk TO da_enf_override_enforcer_id_fk;

ALTER TABLE enforcements
ADD CONSTRAINT enf_result_id_fk FOREIGN KEY 
(
  result_id 
)
REFERENCES results
(
  result_id 
);

ALTER TABLE committal_warrant_progress
ADD CONSTRAINT cwp_enforcement_id_fk FOREIGN KEY
(
  enforcement_id 
)
REFERENCES enforcements
(
  enforcement_id 
);

ALTER TABLE impositions
ADD CONSTRAINT imp_result_id_fk FOREIGN KEY
(
  result_id 
)
REFERENCES results
(
  result_id 
);

ALTER TABLE result_documents
ADD CONSTRAINT rd_result_id_fk FOREIGN KEY
(
  result_id 
)
REFERENCES results
(
  result_id 
);
