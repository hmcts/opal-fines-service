/**
* CGI OPAL Program
*
* MODULE      : defendant_transactions.sql
*
* DESCRIPTION : Creates the DEFENDANT_TRANSACTIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the DEFENDANT_TRANSACTIONS table for the Fines model
*
**/
CREATE TABLE defendant_transactions 
(
 defendant_transaction_id       bigint     not null
,defendant_account_id           bigint     not null
,posted_date                    timestamp  not null
,posted_by                      varchar(20)  
,transaction_type               varchar(6)
,transaction_amount             decimal(18,2)
,payment_method                 varchar(2)
,payment_reference              varchar(10)
,text                           varchar(50)
,status                         varchar(1)
,status_date                    timestamp
,status_amount                  decimal(18,2)
,write_off_code                 varchar(6) 
,associated_record_type         varchar(30)
,associated_record_id           varchar(30)
,imposed_amount                 decimal(18,2)
,CONSTRAINT defendant_transactions_pk PRIMARY KEY 
 (
   defendant_transaction_id	
 ) 
);

ALTER TABLE defendant_transactions
ADD CONSTRAINT dtr_defendant_account_id_fk FOREIGN KEY
(
  defendant_account_id 
)
REFERENCES defendant_accounts
(
  defendant_account_id 
);

COMMENT ON COLUMN defendant_transactions.defendant_transaction_id IS 'Unique ID of this record';
COMMENT ON COLUMN defendant_transactions.defendant_account_id IS 'ID of the account this record belongs to';
COMMENT ON COLUMN defendant_transactions.posted_date IS 'The date the record was posted to the account';
COMMENT ON COLUMN defendant_transactions.posted_by IS 'ID of user responsible for posting this record';
COMMENT ON COLUMN defendant_transactions.transaction_type IS 'The code that determines the type of transaction';
COMMENT ON COLUMN defendant_transactions.transaction_amount IS 'Transaction amount';
COMMENT ON COLUMN defendant_transactions.payment_method IS 'The method of paying, NC (Notes & Coins), CQ (Cheque) CT (Credit Transfer), PO (Postal Order)';
COMMENT ON COLUMN defendant_transactions.payment_reference IS 'Cheque of bacs payment reference';
COMMENT ON COLUMN defendant_transactions.text IS 'Other information associated with this transaction such as a reason for creating it or a third-party name.';
COMMENT ON COLUMN defendant_transactions.status IS 'Indicates if a transaction has been Reversed (R), partially-reversed (P), dishonoured (D), cancelled (X) or cleared/presented (C).';
COMMENT ON COLUMN defendant_transactions.status_date IS 'Indicates the date the status was set, if known.';
COMMENT ON COLUMN defendant_transactions.status_amount IS 'The applicable amount, if the status does not apply to the full transaction amount. For example, where this transaction has been partially reversed the amount, the amount reversed so far. Reversed should be TRUE if reversed_amount = amount.';
COMMENT ON COLUMN defendant_transactions.write_off_code IS 'Code of write-off category applicable if this is a WRTOFF or TFO';
COMMENT ON COLUMN defendant_transactions.associated_record_type IS 'Type of record that is identified by associated_record_id (suspense_transaction, imposition, cheque)';
COMMENT ON COLUMN defendant_transactions.associated_record_id IS 'ID or other reference/number of an associated record. This could be the ID of a suspense transaction if this transaction is a transfer to or from suspense (FR-SUS, REPSUS, XFER, MADJ), or the ID of the affected imposition if this is a Reversal (REVPAY) or (DISHCQ). a reference number of a cheque, or BACS payment which may be deleted before this transaction, or any other third party reference.';
COMMENT ON COLUMN defendant_transactions.imposed_amount IS 'Additional amount imposed by this transaction. Currently only applies to CONSOL.';
