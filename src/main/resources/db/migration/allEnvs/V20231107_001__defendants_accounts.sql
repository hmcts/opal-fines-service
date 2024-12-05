/**
* CGI OPAL Program
*
* MODULE      : defendant_accounts.sql
*
* DESCRIPTION : Creates the DEFENDANT_ACCOUNTS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 09/11/2023    A Dennis    1.0         PO-39 Creates the DEFENDANT_ACCOUNTS table for the Fines model
*
**/
CREATE TABLE defendant_accounts 
(
 defendant_account_id	              bigint  not null
,business_unit_id	                  smallint
,account_number	                    varchar(20)
,imposed_hearing_date	              timestamp
,imposing_court_id	                bigint
,amount_imposed	                    decimal(18,2)
,amount_paid	                      decimal(18,2)
,account_balance	                  decimal(18,2)
,account_status	                    varchar(2)
,completed_date	                    timestamp
,enforcing_court_id	                bigint
,last_hearing_court_id	            bigint
,last_hearing_date	                timestamp
,last_movement_date	                timestamp
,last_enforcement	                  varchar(6)
,last_changed_date	                timestamp
,originator_name	                  varchar(100)
,originator_reference	              varchar(40)
,originator_type	                  varchar(10)
,allow_writeoffs	                  boolean
,allow_cheques	                    boolean
,cheque_clearance_period	          smallint
,credit_transfer_clearance_period	  smallint
,enforcement_override_result_id	    varchar(10)
,enforcement_override_enforcer_id	  bigint
,enforcement_override_tfo_lja_id	  smallint
,unit_fine_detail	                  varchar(100)
,unit_fine_value	                  decimal(18,2)
,collection_order	                  boolean
,collection_order_effective_date	  timestamp
,further_steps_notice_date	        timestamp
,confiscation_order_date	          timestamp
,fine_registration_date	            timestamp
,suspended_committal_enforcement_id	bigint
,consolidated_account_type	        varchar(1)
,payment_card_requested	            boolean
,payment_card_requested_date	      timestamp
,payment_card_requested_by	        varchar(20)
,prosecutor_case_reference	        varchar(40)
,enforcement_case_status	          varchar(10)
,CONSTRAINT defendant_account_id_pk PRIMARY KEY 
 (
   defendant_account_id	
 ) 
);
COMMENT ON COLUMN defendant_accounts.defendant_account_id IS 'Unique ID of this record';
COMMENT ON COLUMN defendant_accounts.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN defendant_accounts.account_number IS 'Account number unique within the business unit';
COMMENT ON COLUMN defendant_accounts.imposed_hearing_date IS 'Date the financial penalties were imposed by court';
COMMENT ON COLUMN defendant_accounts.imposing_court_id IS 'ID of the court that imposed the financial penalties';
COMMENT ON COLUMN defendant_accounts.amount_imposed IS 'The total amount of impositions against this account';
COMMENT ON COLUMN defendant_accounts.amount_paid IS 'The total amount of payments received for this account';
COMMENT ON COLUMN defendant_accounts.account_balance IS 'The balance outstanding on this account';
COMMENT ON COLUMN defendant_accounts.account_status IS 'The status of the account. L (Live), C-(Completed), TO (Transfer Out Pending), TS (Transfer Out to NI/Scotland Pending), TA (Transfer Out Acknowledged), Consolidated (CS).';
COMMENT ON COLUMN defendant_accounts.completed_date IS 'Date the account balance was cleared. Not set by GoB Consolidation as it duplicates the account elsewhere. Going forward new consolidation does not need to do this as it can link multiple accounts';
COMMENT ON COLUMN defendant_accounts.enforcing_court_id IS 'ID of the court responsible for enforcing this account';
COMMENT ON COLUMN defendant_accounts.last_hearing_court_id IS 'ID of the court where the last hearing relating to this account was heard';
COMMENT ON COLUMN defendant_accounts.last_hearing_date IS 'The last date a case relating to this account was heard to court';
COMMENT ON COLUMN defendant_accounts.last_movement_date IS 'The last date there was movement against this account. A movement is considered to be account creation, hearing validation, enforcement, or any change to the account status, paid amount of payment terms.';
COMMENT ON COLUMN defendant_accounts.last_enforcement IS 'The last (or currently in force) enforcement action on this account. Not necessarily the most recent enforcement as some do not update this.';
COMMENT ON COLUMN defendant_accounts.last_changed_date IS 'The date this account was last modified by Account Maintenance';
COMMENT ON COLUMN defendant_accounts.originator_name IS 'The name of the court or system where the account came from';
COMMENT ON COLUMN defendant_accounts.originator_reference IS 'The originator''s reference for this account. This could be the case reference or the trasferring courts reference.';
COMMENT ON COLUMN defendant_accounts.originator_type IS 'How the account got loaded';
COMMENT ON COLUMN defendant_accounts.allow_writeoffs IS 'Whether cheque payments are accepted for this account';
COMMENT ON COLUMN defendant_accounts.allow_cheques IS 'Whether cheque payments are accepted for this account';
COMMENT ON COLUMN defendant_accounts.cheque_clearance_period IS 'The number of days before cheque payments are considered cleared';
COMMENT ON COLUMN defendant_accounts.credit_transfer_clearance_period IS 'The number of days before creditor transfer payments are considered cleared';
COMMENT ON COLUMN defendant_accounts.enforcement_override_result_id IS 'The enforcement result that will be applied if this account is enforcement by auto-enforcement';
COMMENT ON COLUMN defendant_accounts.enforcement_override_enforcer_id IS 'The enforcer that will be allocated to enforcement override result';
COMMENT ON COLUMN defendant_accounts.enforcement_override_tfo_lja_id IS 'The ID of the LJA the account will be transferred to if the override result is TFFOUT';
COMMENT ON COLUMN defendant_accounts.unit_fine_detail IS 'Unit fine calculation information used to calculate the fine amount';
COMMENT ON COLUMN defendant_accounts.unit_fine_value IS 'The unit value used in the fine amount calculation';
COMMENT ON COLUMN defendant_accounts.collection_order IS 'Whether the account is a collection order';
COMMENT ON COLUMN defendant_accounts.collection_order_effective_date IS 'The date the collection order status last changed';
COMMENT ON COLUMN defendant_accounts.further_steps_notice_date IS 'The date a Further Steps Notice was first issued for this account';
COMMENT ON COLUMN defendant_accounts.confiscation_order_date IS 'The date a Confiscation Order was first issued for this account';
COMMENT ON COLUMN defendant_accounts.fine_registration_date IS 'The date a Registration of Fine was first made for this account';
COMMENT ON COLUMN defendant_accounts.suspended_committal_enforcement_id IS 'ID of the suspended committal enforcement action that was first applied to this account';
COMMENT ON COLUMN defendant_accounts.consolidated_account_type IS 'If the account has been subject to a consolidation. Master or Child.';
COMMENT ON COLUMN defendant_accounts.payment_card_requested IS 'Whether a payment card has been requested for this account';
COMMENT ON COLUMN defendant_accounts.payment_card_requested_date IS 'The date a payment card was last request for this account';
COMMENT ON COLUMN defendant_accounts.payment_card_requested_by IS 'The ID of the user that requested a payment card for this account';
COMMENT ON COLUMN defendant_accounts.prosecutor_case_reference IS 'The reference from the prosecuting authority';
COMMENT ON COLUMN defendant_accounts.enforcement_case_status IS 'Status of an enforcement case creation request to Common Platform';
