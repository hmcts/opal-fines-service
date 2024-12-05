/**
* OPAL Program
*
* MODULE      : bacs_payments.sql
*
* DESCRIPTION : Create the BACS_PAYMENTS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-395 Create the BACS_PAYMENTS table in the Fines model
*
**/ 

CREATE TABLE bacs_payments
(
 bacs_payment_id            bigint
,business_unit_id           smallint        not null 
,bacs_number                bigint          not null
,issue_date                 timestamp       not null
,creditor_transaction_id    bigint
,defendant_transaction_id   bigint
,amount                     decimal(18,2)   not null
,status                     varchar(10)     not null
,CONSTRAINT bacs_payments_pk PRIMARY KEY (bacs_payment_id) 
,CONSTRAINT bacs_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
,CONSTRAINT bacs_creditor_transaction_id_fk FOREIGN KEY (creditor_transaction_id) REFERENCES creditor_transactions (creditor_transaction_id)
,CONSTRAINT bacs_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES defendant_transactions (defendant_transaction_id) 
);

COMMENT ON COLUMN bacs_payments.bacs_payment_id IS 'Unique ID of this record';
COMMENT ON COLUMN bacs_payments.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN bacs_payments.bacs_number IS 'BACS schema level sequence number';
COMMENT ON COLUMN bacs_payments.issue_date IS 'Issue date';
COMMENT ON COLUMN bacs_payments.creditor_transaction_id IS 'ID of the relating creditor transaction';
COMMENT ON COLUMN bacs_payments.defendant_transaction_id IS 'ID of the relating defendant transaction';
COMMENT ON COLUMN bacs_payments.amount IS 'Payment amount';
COMMENT ON COLUMN bacs_payments.status IS 'BACS status';