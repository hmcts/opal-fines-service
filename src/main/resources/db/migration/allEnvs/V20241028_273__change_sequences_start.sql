/**
* OPAL Program
*
* MODULE      : change_sequences_start.sql
*
* DESCRIPTION : Recreate primary key sequences to start at 50000000000000 as IDs lower than that are reserved for data loaded from then Legacy GoB database. NOTES will start from 60000000000000
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 28/10/2024    A Dennis    1.0         PO-928  Recreate primary key sequences to start at 50000000000000 as IDs lower than that are reserved for data loaded from then Legacy GoB database. NOTES will start from 60000000000000.
*
**/

DROP sequence IF EXISTS alias_id_seq;
CREATE SEQUENCE IF NOT EXISTS alias_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY aliases.alias_id;

DROP sequence IF EXISTS allocation_id_seq;
CREATE SEQUENCE IF NOT EXISTS allocation_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY allocations.allocation_id;

DROP sequence IF EXISTS control_total_id_seq;
CREATE SEQUENCE IF NOT EXISTS control_total_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY control_totals.control_total_id;

DROP sequence IF EXISTS creditor_account_id_seq;
CREATE SEQUENCE IF NOT EXISTS creditor_account_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY creditor_accounts.creditor_account_id;

DROP sequence IF EXISTS defendant_account_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_account_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY defendant_accounts.defendant_account_id;

DROP sequence IF EXISTS defendant_account_party_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_account_party_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY defendant_account_parties.defendant_account_party_id;

DROP sequence IF EXISTS defendant_transaction_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_transaction_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY defendant_transactions.defendant_transaction_id;

DROP sequence IF EXISTS document_instance_id_seq;
CREATE SEQUENCE IF NOT EXISTS document_instance_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY document_instances.document_instance_id;

DROP sequence IF EXISTS enforcement_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY enforcements.enforcement_id;

DROP sequence IF EXISTS imposition_id_seq;
CREATE SEQUENCE IF NOT EXISTS imposition_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY impositions.imposition_id;

DROP sequence IF EXISTS note_id_seq;
CREATE SEQUENCE IF NOT EXISTS note_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY notes.note_id;

DROP sequence IF EXISTS party_id_seq;
CREATE SEQUENCE IF NOT EXISTS party_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY parties.party_id;

DROP sequence IF EXISTS payment_terms_id_seq;
CREATE SEQUENCE IF NOT EXISTS payment_terms_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY payment_terms.payment_terms_id;

DROP sequence IF EXISTS report_entry_id_seq;
CREATE SEQUENCE IF NOT EXISTS report_entry_id_seq INCREMENT 1 MINVALUE 50000000000000 NO MAXVALUE START WITH 50000000000000 CACHE 20 OWNED BY report_entries.report_entry_id;
