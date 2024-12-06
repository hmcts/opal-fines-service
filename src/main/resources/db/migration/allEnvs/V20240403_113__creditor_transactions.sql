/**
* CGI OPAL Program
*
* MODULE      : creditor_transactions.sql
*
* DESCRIPTION : Creates the CREDITOR_TRANSACTIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the CREDITOR_TRANSACTIONS table for the Fines model
*
**/
CREATE TABLE creditor_transactions 
(
 creditor_transactions_id      bigint         not null
,creditor_account_id           bigint         not null
,posted_date                   timestamp      not null
,posted_by                     varchar(20)
,posted_by_user_id             bigint         not null
,transaction_type              varchar(6)     not null
,transaction_amount            decimal(18,2)  not null
,imposition_result_id          varchar(10)   
,payment_processed             boolean
,payment_reference             varchar(10)
,status                        varchar(1)
,status_date                   timestamp
,associated_record_type        varchar(30)
,associated_record_id          varchar(30)
,CONSTRAINT creditor_transactions_pk PRIMARY KEY 
 (
  creditor_transactions_id	
 )  
);

ALTER TABLE creditor_transactions
ADD CONSTRAINT ct_creditor_account_id_fk FOREIGN KEY
(
  creditor_account_id 
)
REFERENCES creditor_accounts
(
  creditor_account_id 
);

ALTER TABLE creditor_transactions
ADD CONSTRAINT ct_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

COMMENT ON COLUMN creditor_transactions.creditor_transactions_id IS 'Unique ID of this record';
COMMENT ON COLUMN creditor_transactions.creditor_account_id IS 'ID of the creditor account this record belongs to';
COMMENT ON COLUMN creditor_transactions.posted_date IS 'The date the record was posted to the account';
COMMENT ON COLUMN creditor_transactions.posted_by IS 'The ID of the user that posted this transaction';
COMMENT ON COLUMN creditor_transactions.posted_by_user_id IS 'The user ID and is the foreign key to Users table but can be NULL, so if a not null value is put then it is enforced.';
COMMENT ON COLUMN creditor_transactions.transaction_type IS 'The code that determines the type of transaction';
COMMENT ON COLUMN creditor_transactions.transaction_amount IS 'Transaction amount';
COMMENT ON COLUMN creditor_transactions.imposition_result_id IS 'The imposition result this transaction is in respect of';
COMMENT ON COLUMN creditor_transactions.payment_processed IS 'Indicates if a transaction has been paid by payout or marked as processed by some internal process so payout will ignore it.';
COMMENT ON COLUMN creditor_transactions.payment_reference IS 'The reference number of a cheque or bacs payment';
COMMENT ON COLUMN creditor_transactions.status IS 'Indicates if a transaction has been Reversed (R), partially-reversed (P), dishonoured (D), cancelled (X) or cleared/presented (C).';
COMMENT ON COLUMN creditor_transactions.status_date IS 'Indicates the date the status was set, if known.';
COMMENT ON COLUMN creditor_transactions.associated_record_type IS 'Table where relating record that caused this amount is stored';
COMMENT ON COLUMN creditor_transactions.associated_record_id IS 'ID or other reference/number of an associated record';
