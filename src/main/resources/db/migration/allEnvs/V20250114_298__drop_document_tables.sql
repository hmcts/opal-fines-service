/**
* OPAL Program
*
* MODULE      : drop_document_tables.sql
*
* DESCRIPTION : Drop document related tables so they can be recreated after reference data work done by Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970  Drop document related tables so they can be recreated after reference data work done by Capita
*
**/

ALTER TABLE account_transfers
DROP constraint IF EXISTS at_document_instance_id_fk;

DROP TABLE IF EXISTS document_instances;
DROP SEQUENCE IF EXISTS result_document_id_seq;
DROP TABLE IF EXISTS result_documents;
DROP TABLE IF EXISTS documents;
