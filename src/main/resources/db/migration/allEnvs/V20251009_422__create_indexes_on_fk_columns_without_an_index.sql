/**
* CGI OPAL Program
*
* MODULE      : create_indexes_on_fk_columns_without_an_index.sql
*
* DESCRIPTION : Create indexes on all foreign key columns that currently do not have an index
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------
* 03/10/2025    P Brumby    1.1         PO-2121 - Create indexes on all foreign key columns that currently do not have an index
*
**/

-- Create new indexes for the following 78 forign keys where index does not exist

-- account_transfers.defendant_account_id	 at_defendant_account_id_fk
CREATE INDEX at_defendant_account_id_idx ON account_transfers (defendant_account_id);

-- account_transfers.destination_lja_id  at_destination_lja_id_fk
CREATE INDEX at_destination_lja_id_idx ON account_transfers (destination_lja_id);

-- account_transfers.document_instance_id  at_document_instance_id_fk	
CREATE INDEX at_document_instance_id_idx ON account_transfers (document_instance_id);

-- allocations.defendant_transaction_id  all_defendant_transaction_id_fk
CREATE INDEX all_defendant_transaction_id_idx ON allocations (defendant_transaction_id);

-- allocations.imposition_id  all_imposition_id_fk
CREATE INDEX all_imposition_id_idx ON allocations (imposition_id);

-- amendments.business_unit_id  amdt_business_unit_id_fk
CREATE INDEX amdt_business_unit_id_idx ON amendments (business_unit_id);

-- amendments.field_code  amend_field_code_fk
CREATE INDEX amdt_field_code_idx ON amendments (field_code);

-- bacs_payments.business_unit_id  bacs_business_unit_id_fk
CREATE INDEX bacs_business_unit_id_idx ON bacs_payments (business_unit_id);

-- bacs_payments creditor_transaction_id  bacs_creditor_transaction_id_fk
CREATE INDEX bacs_creditor_transaction_id_idx ON bacs_payments (creditor_transaction_id);

-- bacs_payments.defendant_transaction_id  bacs_defendant_transaction_id_fk
CREATE INDEX bacs_defendant_transaction_id_idx ON bacs_payments (defendant_transaction_id);

-- business_unit_users.business_unit_id buu_business_unit_id_fk
CREATE INDEX buu_business_unit_id_idx ON business_unit_users (business_unit_id);

-- business_unit_users.user_id  buu_user_id_fk
CREATE INDEX buu_user_id_idx ON business_unit_users (user_id);

-- cheques.business_unit_id  che_business_unit_id_fk
CREATE INDEX che_business_unit_id_idx ON cheques (business_unit_id);

-- cheques.creditor_transaction_id	 che_creditor_transaction_id_fk
CREATE INDEX che_creditor_transaction_id_idx ON cheques (creditor_transaction_id);

-- cheques.defendant_transaction_id  che_defendant_transaction_id_fk
CREATE INDEX che_defendant_transaction_id_idx ON cheques (defendant_transaction_id);

-- committal_warrant_progress.defendant_account_id  cwp_defendant_account_id_fk
CREATE INDEX cwp_defendant_account_id_idx ON committal_warrant_progress (defendant_account_id);

-- committal_warrant_progress.enforcement_id  cwp_enforcement_id_fk
CREATE INDEX cwp_enforcement_id_idx ON committal_warrant_progress (enforcement_id);

-- committal_warrant_progress.prison_id  enf_prison_id_fk
CREATE INDEX cwp_prison_id_idx ON committal_warrant_progress (prison_id);

-- control_totals.business_unit_id  ct_business_unit_id_fk
CREATE INDEX ct_business_unit_id_idx ON control_totals (business_unit_id);

-- control_totals.ct_report_instance_id  ct_ct_report_instance_id_fk
CREATE INDEX ct_ct_report_instance_id_idx ON control_totals (ct_report_instance_id);

-- control_totals.qe_report_instance_id  ct_qe_report_instance_id_fk
CREATE INDEX ct_qe_report_instance_id_idx ON control_totals (qe_report_instance_id);

-- court_fees.business_unit_id  cf_business_unit_id_fk
CREATE INDEX cf_business_unit_id_idx ON court_fees (business_unit_id);

-- court_fees_received.business_unit_id  cfr_business_unit_id_fk
CREATE INDEX cfr_received_business_unit_id_idx ON court_fees_received (business_unit_id);

-- court_fees_received.court_fee_id  cfr_court_fee_id_fk
CREATE INDEX cfr_court_fee_id_idx ON court_fees_received (court_fee_id);

-- court_fees_received.suspense_transaction_id  cfr_suspense_transaction_id_fk
CREATE INDEX cfr_suspense_transaction_id_idx ON court_fees_received (suspense_transaction_id);

-- courts.business_unit_id  crt_business_unit_id_fk
CREATE INDEX crt_business_unit_id_idx ON courts (business_unit_id);

-- courts.local_justice_area_id  crt_local_justice_area_id_fk
CREATE INDEX crt_local_justice_area_id_idx ON courts (local_justice_area_id);

-- courts.parent_court_id  crt_parent_court_id_fk
CREATE INDEX crt_parent_court_id_idx ON courts (parent_court_id);

-- creditor_accounts	major_creditor_id  ca_major_creditor_id_fk
CREATE INDEX ca_major_creditor_id_idx ON creditor_accounts (major_creditor_id);

-- defendant_accounts.enf_override_enforcer_id  da_enf_override_enforcer_id_fk
CREATE INDEX da_enf_override_enforcer_id_idx ON defendant_accounts (enf_override_enforcer_id);

-- defendant_accounts.enf_override_result_id  da_enf_override_result_id_fk
CREATE INDEX da_enf_override_result_id_idx ON defendant_accounts (enf_override_result_id);

-- defendant_accounts.enf_override_tfo_lja_id  da_enf_override_tfo_lja_id_fk
CREATE INDEX da_enf_override_tfo_lja_id_idx ON defendant_accounts (enf_override_tfo_lja_id);

-- defendant_accounts.enforcing_court_id  da_enforcing_court_id_fk
CREATE INDEX da_enforcing_court_id_idx ON defendant_accounts (enforcing_court_id);

-- defendant_accounts.imposing_court_id  da_imposing_court_id_fk
CREATE INDEX da_imposing_court_id_idx ON defendant_accounts (imposing_court_id);

-- defendant_accounts.last_hearing_court_id  da_last_hearing_court_id_fk
CREATE INDEX da_last_hearing_court_id_idx ON defendant_accounts (last_hearing_court_id);

-- defendant_transactions.defendant_account_id  dtr_defendant_account_id_fk
CREATE INDEX dtr_defendant_account_id_idx ON defendant_transactions (defendant_account_id);

-- enforcement_account_types.business_unit_id  eat_business_unit_id_fk
CREATE INDEX eat_business_unit_id_idx ON enforcement_account_types (business_unit_id);

-- enforcement_path_sets.business_unit_id  eps_business_unit_id_fk
CREATE INDEX eps_business_unit_id_idx ON enforcement_path_sets (business_unit_id);

-- enforcement_paths.enforcement_account_type_id  ep_enforcement_account_type_id_fk
CREATE INDEX ep_enforcement_account_type_id_idx ON enforcement_paths (enforcement_account_type_id);

-- enforcement_paths.enforcement_path_set_id  ep_enforcement_path_set_id_fk
CREATE INDEX ep_enforcement_path_set_id_idx ON enforcement_paths (enforcement_path_set_id);

-- enforcement_run_courts.court_id  erc_court_id_fk
CREATE INDEX erc_court_id_idx ON enforcement_run_courts (court_id);

-- enforcement_run_courts.enforcement_run_id  erc_enforcement_run_id_fk
CREATE INDEX erc_enforcement_run_id_idx ON enforcement_run_courts (enforcement_run_id);

-- enforcement_runs.business_unit_id  es_business_unit_id_fk
CREATE INDEX es_business_unit_id_idx ON enforcement_runs (business_unit_id);

-- enforcements.defendant_account_id  enf_defendant_account_id_fk
CREATE INDEX enf_defendant_account_id_idx ON enforcements (defendant_account_id);

-- enforcements.enforcer_id  enf_enforcer_id_fk
CREATE INDEX enf_enforcer_id_idx ON enforcements (enforcer_id);

-- enforcements.hearing_court_id  enf_hearing_court_id_fk
CREATE INDEX enf_hearing_court_id ON enforcements (hearing_court_id);

-- enforcements.result_id  enf_result_id_fk
CREATE INDEX enf_result_id_idx ON enforcements (result_id);

-- enforcer_allocations.enforcer_id  ea_enforcer_id_fk
CREATE INDEX ea_allocations_enforcer_id_idx ON enforcer_allocations (enforcer_id);

-- enforcer_allocations.result_id  ea_result_id_fk
CREATE INDEX ea_result_id_idx ON enforcer_allocations (result_id);

-- enforcers.business_unit_id  enf_business_unit_id_fk
CREATE INDEX efs_business_unit_id_idx ON enforcers (business_unit_id);

-- hmrc_requests.business_unit_id  hr_business_unit_id_fk
CREATE INDEX hr_business_unit_id_idx ON hmrc_requests (business_unit_id);

-- impositions.imposing_court_id  imp_imposing_court_id_fk
CREATE INDEX imp_imposing_court_id_idx ON impositions (imposing_court_id);

-- impositions.offence_id  imp_offence_id_fk
CREATE INDEX imp_offence_id_idx ON impositions (offence_id);

-- impositions.original_imposition_id  imp_original_imposition_id_fk
CREATE INDEX imp_original_imposition_id_idx ON impositions (original_imposition_id);

-- impositions.result_id  imp_result_id_fk
CREATE INDEX imp_result_id_idx ON impositions (result_id);

-- interface_jobs.business_unit_id  if_business_unit_id_fk
CREATE INDEX ij_business_unit_id_idx ON interface_jobs (business_unit_id);

-- log_audit_details.business_unit_id  lad_business_unit_id_fk
CREATE INDEX lad_business_unit_id_idx ON log_audit_details (business_unit_id);

-- log_audit_details.log_action_id  lad_log_action_id_fk
CREATE INDEX lad_log_action_id_idx ON log_audit_details (log_action_id);

-- log_audit_details.user_id  lad_user_id_fk
CREATE INDEX lad_user_id_idx ON log_audit_details (user_id);

-- major_creditors.business_unit_id  mc_business_unit_id_fk
CREATE INDEX mc_business_unit_id_idx ON major_creditors (business_unit_id);

-- miscellaneous_accounts.party_id  ma_party_id_fk
CREATE INDEX ma_party_id_idx ON miscellaneous_accounts (party_id);

-- offences.business_unit_id  off_business_unit_id_fk
CREATE INDEX off_business_unit_id_idx ON offences (business_unit_id);

-- prisons.business_unit_id  pri_business_unit_id_fk
CREATE INDEX pri_business_unit_id_idx ON prisons (business_unit_id);

-- report_entries.business_unit_id  re_business_unit_id_fk
CREATE INDEX re_business_unit_id_idx ON report_entries (business_unit_id);

-- report_entries.report_id  re_report_id_fk
CREATE INDEX re_report_id_idx ON report_entries (report_id);

-- report_entries.report_instance_id  re_report_instance_id_fk
CREATE INDEX re_report_instance_id_idx ON report_entries (report_instance_id);

-- report_instances.business_unit_id  ri_business_unit_id_fk
CREATE INDEX ri_business_unit_id_idx ON report_instances (business_unit_id);

-- report_instances.report_id  ri_report_id_fk
CREATE INDEX ri_report_id_idx ON report_instances (report_id);

-- result_documents.cy_document_id  rd_cy_document_id_fk
CREATE INDEX rd_cy_document_id_idx ON result_documents (cy_document_id);

-- standard_letters.business_unit_id  sl_business_unit_id_fk
CREATE INDEX sl_business_unit_id_idx ON standard_letters (business_unit_id);

-- suspense_accounts.business_unit_id  sa_business_unit_id_fk
CREATE INDEX sa_business_unit_id_idx ON suspense_accounts (business_unit_id);

-- suspense_items.court_fee_id  si_court_fee_id_fk
CREATE INDEX si_court_fee_id_idx ON suspense_items (court_fee_id);

-- suspense_items.suspense_account_id  si_suspense_account_id_fk
CREATE INDEX si_suspense_account_id_idx ON suspense_items (suspense_account_id);

-- suspense_transactions.suspense_item_id  st_suspense_item_id_fk
CREATE INDEX st_suspense_item_id_idx ON suspense_transactions (suspense_item_id);

-- tills.business_unit_id  till_business_unit_id_fk
CREATE INDEX till_business_unit_id_idx ON tills (business_unit_id);

-- user_entitlements.application_function_id  ue_application_function_id_fk
CREATE INDEX ue_application_function_id_idx ON user_entitlements (application_function_id);

-- user_entitlements.business_unit_user_id  ue_business_unit_user_id_fk
CREATE INDEX ue_business_unit_user_id_idx ON user_entitlements (business_unit_user_id);

-- warrant_register.business_unit_id  wr_business_unit_id_fk
CREATE INDEX wr_business_unit_id_idx ON warrant_register (business_unit_id);

-- Create new indexes for the following tables where the current existing indexes do not support the forign key columns.

-- payment_terms  pt_defendant_account_id_fk - the existing index pt_def_acc_id_active_udx is partial therefore create a new fk index
CREATE INDEX pt_defendant_account_id_idx ON payment_terms (defendant_account_id);

-- configuration_items  ci_business_unit_id_fk - the order of existing multi-column index ci_item_name_bu_idx does not support business_unit_id therefore create a new fk index
CREATE INDEX ci_business_unit_id_idx ON configuration_items (business_unit_id);

-- document_instances  di_document_id_fk - the order of existing multi-column index di_bu_document_status_date_idx does not support document_id therefore create a new fk index
CREATE INDEX di_document_id_idx ON document_instances (document_id);

-- result_documents  rd_document_id_fk - the order of existing multi-column index rd_result_document_idx does not support document_id therefore create a new fk index
CREATE INDEX rd_document_id_idx ON result_documents (document_id);

-- template_mappings  tm_application_function_fk - the order of existing multi-column primary key index template_mappings_pk does not support application_function_id therefore create a new fk index
CREATE INDEX tm_application_function_idx ON template_mappings (application_function_id);