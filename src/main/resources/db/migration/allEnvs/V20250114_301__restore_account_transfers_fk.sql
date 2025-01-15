/**
* OPAL Program
*
* MODULE      : restore_account_transfers_fk.sql
*
* DESCRIPTION : Restore foreign key in ACCOUNT_TRANSFERS table after DOCUMENT_INSTANCES table was recreated
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970  Restore foreign key in ACCOUNT_TRANSFERS table after DOCUMENT_INSTANCES table was recreated
*
**/

ALTER TABLE account_transfers
ADD CONSTRAINT at_document_instance_id_fk FOREIGN KEY
(
  document_instance_id 
)
REFERENCES document_instances
(
  document_instance_id 
);
