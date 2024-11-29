/**
* CGI OPAL Program
*
* MODULE      : payment_terms.sql
*
* DESCRIPTION : Creates the PAYMENT_TERMS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the PAYMENT_TERMS table for the Fines model
*
**/
CREATE TABLE payment_terms 
(
 payment_terms_id              bigint      not null
,defendant_account_id          bigint      not null
,posted_date                   timestamp   not null
,posted_by                     varchar(20)
,terms_type_code               varchar(1)  not null
,effective_date                timestamp
,instalment_period             varchar(1)
,instalment_amount             decimal(18,2)
,instalment_lump_sum           decimal(18,2)
,jail_days                     integer
,extension                     boolean
,account_balance               decimal(18,2)
,CONSTRAINT payment_terms_pk PRIMARY KEY 
 (
   payment_terms_id	
 ) 
);

ALTER TABLE payment_terms
ADD CONSTRAINT pt_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

COMMENT ON COLUMN payment_terms.payment_terms_id IS 'Unique ID of this record';
COMMENT ON COLUMN payment_terms.defendant_account_id IS 'ID of the account this record belongs to';
COMMENT ON COLUMN payment_terms.posted_date IS 'The date the record was posted to the account';
COMMENT ON COLUMN payment_terms.posted_by IS 'ID of user responsible for posting this record';
COMMENT ON COLUMN payment_terms.terms_type_code IS 'The terms type: By Date (B), Paid (P), Instalments (I)';
COMMENT ON COLUMN payment_terms.effective_date IS 'the date when the full amount is due or when instalments start';
COMMENT ON COLUMN payment_terms.instalment_period IS 'W (Week), M (Month), F (Fortnight) or NULL if not instalments';
COMMENT ON COLUMN payment_terms.instalment_amount IS 'Amount due each period if paying by instalments';
COMMENT ON COLUMN payment_terms.instalment_lump_sum IS 'An Initial lumpsum that is due before instalments start';
COMMENT ON COLUMN payment_terms.jail_days IS 'Number of days in jail the defendant will spend in default of payment';
COMMENT ON COLUMN payment_terms.extension IS 'If this is an extension to existing payment terms';
COMMENT ON COLUMN payment_terms.account_balance IS 'Account balance at the time of posting thes terms';
