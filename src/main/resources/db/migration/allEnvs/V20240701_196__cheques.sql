/**
* OPAL Program
*
* MODULE      : cheques.sql
*
* DESCRIPTION : Create the CHEQUES table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-394 Create the CHEQUES table in the Fines model
*
**/ 

CREATE TABLE cheques
(
 cheque_id                  bigint
,business_unit_id           smallint        not null 
,cheque_number              bigint          not null
,issue_date                 timestamp       not null
,creditor_transaction_id    bigint
,defendant_transaction_id   bigint
,amount                     decimal(18,2)   not null
,allocation_type            varchar(10)
,reminder_date              timestamp
,status                     varchar(1)      not null
,CONSTRAINT cheques_pk PRIMARY KEY (cheque_id) 
,CONSTRAINT che_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
,CONSTRAINT che_creditor_transaction_id_fk FOREIGN KEY (creditor_transaction_id) REFERENCES creditor_transactions (creditor_transaction_id)
,CONSTRAINT che_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES defendant_transactions (defendant_transaction_id) 
);

COMMENT ON COLUMN cheques.cheque_id IS 'Unique ID of this record';
COMMENT ON COLUMN cheques.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN cheques.cheque_number IS 'Business unit level cheque number';
COMMENT ON COLUMN cheques.issue_date IS 'Issue date';
COMMENT ON COLUMN cheques.creditor_transaction_id IS 'ID of the relating creditor transaction';
COMMENT ON COLUMN cheques.defendant_transaction_id IS 'ID of the relating defendant transaction';
COMMENT ON COLUMN cheques.amount IS 'Payment amount';
COMMENT ON COLUMN cheques.allocation_type IS 'Indicates what this cheque payment is in respect of, for example, COMP or REPAYW.';
COMMENT ON COLUMN cheques.reminder_date IS 'The date a reminder letter to present the cheque was sent to the creditor';
COMMENT ON COLUMN cheques.status IS 'The cheque status. Values: N (new), D (destroyed), P (presented), Q (query - presented different amount), W (Withdrawn), X (Awaiting deletion)';