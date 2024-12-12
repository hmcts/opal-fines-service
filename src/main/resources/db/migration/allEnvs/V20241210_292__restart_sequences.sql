/**
* OPAL Program
*
* MODULE      : restart_sequences.sql
*
* DESCRIPTION : Recreate primary key sequences to start at 60000000000000 to avoid clash with Legacy and make it easier to idedntify post GoB keys.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------
* 10/12/2024    A Dennis    1.0         PO-962  Recreate primary key sequences to start at 60000000000000 to avoid clash with Legacy and make it easier to idedntify post GoB keys.
*
**/

DROP sequence IF EXISTS account_transfer_id_seq;
CREATE SEQUENCE IF NOT EXISTS account_transfer_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY account_transfers.account_transfer_id;

DROP sequence IF EXISTS alias_id_seq;
CREATE SEQUENCE IF NOT EXISTS alias_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY aliases.alias_id;

DROP sequence IF EXISTS allocation_id_seq;
CREATE SEQUENCE IF NOT EXISTS allocation_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY allocations.allocation_id;

DROP sequence IF EXISTS amendment_id_seq;
CREATE SEQUENCE IF NOT EXISTS amendment_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY amendments.amendment_id;

DROP sequence IF EXISTS bacs_payment_id_seq;
CREATE SEQUENCE IF NOT EXISTS bacs_payment_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY bacs_payments.bacs_payment_id;

DROP sequence IF EXISTS cheque_id_seq;
CREATE SEQUENCE IF NOT EXISTS cheque_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY cheques.cheque_id;

DROP sequence IF EXISTS configuration_item_id_seq;
CREATE SEQUENCE IF NOT EXISTS configuration_item_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY configuration_items.configuration_item_id;

DROP sequence IF EXISTS control_total_id_seq;
CREATE SEQUENCE IF NOT EXISTS control_total_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY control_totals.control_total_id;

DROP sequence IF EXISTS court_fee_id_seq;
CREATE SEQUENCE IF NOT EXISTS court_fee_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY court_fees.court_fee_id;

DROP sequence IF EXISTS court_fee_received_id_seq;
CREATE SEQUENCE IF NOT EXISTS court_fee_received_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY court_fees_received.court_fee_received_id;

DROP sequence IF EXISTS court_id_seq;
CREATE SEQUENCE IF NOT EXISTS court_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY courts.court_id;

DROP sequence IF EXISTS creditor_account_id_seq;
CREATE SEQUENCE IF NOT EXISTS creditor_account_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY creditor_accounts.creditor_account_id;

DROP sequence IF EXISTS creditor_transaction_id_seq;
CREATE SEQUENCE IF NOT EXISTS creditor_transaction_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY creditor_transactions.creditor_transaction_id;

DROP sequence IF EXISTS defendant_account_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_account_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY defendant_accounts.defendant_account_id;

DROP sequence IF EXISTS defendant_account_party_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_account_party_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY defendant_account_parties.defendant_account_party_id;

DROP sequence IF EXISTS defendant_transaction_id_seq;
CREATE SEQUENCE IF NOT EXISTS defendant_transaction_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY defendant_transactions.defendant_transaction_id;

DROP sequence IF EXISTS document_instance_id_seq;
CREATE SEQUENCE IF NOT EXISTS document_instance_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY document_instances.document_instance_id;

DROP sequence IF EXISTS enforcement_account_type_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_account_type_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcement_account_types.enforcement_account_type_id;

DROP sequence IF EXISTS enforcement_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcements.enforcement_id;

DROP sequence IF EXISTS enforcement_path_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_path_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcement_paths.enforcement_path_id;

DROP sequence IF EXISTS enforcement_path_set_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_path_set_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcement_path_sets.enforcement_path_set_id;

DROP sequence IF EXISTS enforcement_run_court_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_run_court_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcement_run_courts.enforcement_run_court_id;

DROP sequence IF EXISTS enforcement_run_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcement_run_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcement_runs.enforcement_run_id;

DROP sequence IF EXISTS enforcer_id_seq;
CREATE SEQUENCE IF NOT EXISTS enforcer_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY enforcers.enforcer_id;

DROP sequence IF EXISTS imposition_id_seq;
CREATE SEQUENCE IF NOT EXISTS imposition_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY impositions.imposition_id;

DROP sequence IF EXISTS major_creditor_id_seq;
CREATE SEQUENCE IF NOT EXISTS major_creditor_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY major_creditors.major_creditor_id;

DROP sequence IF EXISTS miscellaneous_account_id_seq;
CREATE SEQUENCE IF NOT EXISTS miscellaneous_account_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY miscellaneous_accounts.miscellaneous_account_id;

DROP sequence IF EXISTS note_id_seq;
CREATE SEQUENCE IF NOT EXISTS note_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY notes.note_id;

DROP sequence IF EXISTS party_id_seq;
CREATE SEQUENCE IF NOT EXISTS party_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY parties.party_id;

DROP sequence IF EXISTS payment_terms_id_seq;
CREATE SEQUENCE IF NOT EXISTS payment_terms_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY payment_terms.payment_terms_id;

DROP sequence IF EXISTS prison_id_seq;
CREATE SEQUENCE IF NOT EXISTS prison_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY prisons.prison_id;

DROP sequence IF EXISTS report_entry_id_seq;
CREATE SEQUENCE IF NOT EXISTS report_entry_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY report_entries.report_entry_id;

DROP sequence IF EXISTS report_instance_id_seq;
CREATE SEQUENCE IF NOT EXISTS report_instance_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY report_instances.report_instance_id;

DROP sequence IF EXISTS standard_letter_id_seq;
CREATE SEQUENCE IF NOT EXISTS standard_letter_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY standard_letters.standard_letter_id;

DROP sequence IF EXISTS suspense_item_id_seq;
CREATE SEQUENCE IF NOT EXISTS suspense_item_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY suspense_items.suspense_item_id;

DROP sequence IF EXISTS suspense_transaction_id_seq;
CREATE SEQUENCE IF NOT EXISTS suspense_transaction_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY suspense_transactions.suspense_transaction_id;

DROP sequence IF EXISTS warrant_register_id_seq;
CREATE SEQUENCE IF NOT EXISTS warrant_register_id_seq INCREMENT 1 MINVALUE 60000000000000 NO MAXVALUE START WITH 60000000000000 CACHE 20 OWNED BY warrant_register.warrant_register_id;

-- Drop the following sequences if they exist since they do not require a sequence
DROP sequence IF EXISTS suspense_account_id_seq;
DROP sequence IF EXISTS debtor_detail_id_seq;
DROP sequence IF EXISTS local_justice_area_id_seq;
