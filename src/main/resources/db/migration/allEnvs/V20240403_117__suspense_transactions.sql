/**
* CGI OPAL Program
*
* MODULE      : suspense_transactions.sql
*
* DESCRIPTION : Creates the SUSPENSE_TRANSACTIONS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the SUSPENSE_TRANSACTIONS table for the Fines model
*
**/
CREATE TABLE suspense_transactions 
(
 suspense_transaction_id        bigint          not null
,suspense_item_id               bigint          not null
,posted_date                    timestamp       not null
,posted_by                      varchar(20)
,posted_by_user_id              bigint          not null
,transaction_type               varchar(2)      not null
,amount                         decimal(18,2)   not null
,associated_record_type         varchar(30)
,associated_record_id           varchar(30)
,text                           varchar(50)
,reversed                       varchar(1)
,CONSTRAINT suspense_transactions_pk PRIMARY KEY 
 (
  suspense_transaction_id	
 )  
);

ALTER TABLE suspense_transactions
ADD CONSTRAINT st_suspense_item_id_fk FOREIGN KEY
(
  suspense_item_id 
)
REFERENCES suspense_items
(
  suspense_item_id 
);

ALTER TABLE suspense_transactions
ADD CONSTRAINT st_posted_by_user_id_fk FOREIGN KEY
(
  posted_by_user_id 
)
REFERENCES users
(
  user_id 
);

COMMENT ON COLUMN suspense_transactions.suspense_transaction_id IS 'Unique ID of this record';
COMMENT ON COLUMN suspense_transactions.suspense_item_id IS 'The suspense item that this transaction belongs to';
COMMENT ON COLUMN suspense_transactions.posted_date IS 'Date this transaction was posted';
COMMENT ON COLUMN suspense_transactions.posted_by IS 'ID of user that posted this transaction';
COMMENT ON COLUMN suspense_transactions.posted_by_user_id IS 'The user ID and is the foreign key to Users table but can be NULL, so if a not null value is put then it is enforced.';
COMMENT ON COLUMN suspense_transactions.transaction_type IS 'Suspense transaction type';
COMMENT ON COLUMN suspense_transactions.amount IS 'Amount of this transaction';
COMMENT ON COLUMN suspense_transactions.associated_record_type IS 'Type of record identified by associated_record_id';
COMMENT ON COLUMN suspense_transactions.associated_record_id IS 'ID or other reference/number of an associated record';
COMMENT ON COLUMN suspense_transactions.text IS 'Further detail associated with the transaction';
COMMENT ON COLUMN suspense_transactions.reversed IS 'If this transaction has subsequently been reversed (R) or dishonoured (D)';
