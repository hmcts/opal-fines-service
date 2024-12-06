/**
* OPAL Program
*
* MODULE      : court_fees_received.sql
*
* DESCRIPTION : Create the COURT_FEES_RECEIVED table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-393 Create the COURT_FEES_RECEIVED table in the Fines model
*
**/ 

CREATE TABLE court_fees_received
(
 court_fee_received_id    bigint
,business_unit_id         smallint     not null 
,court_fee_id             bigint  
,overpayment              boolean      not null
,suspense_transaction_id  bigint       not null
,transferred_date         timestamp
,number_of_items          smallint     not null
,CONSTRAINT court_fees_received_pk PRIMARY KEY (court_fee_received_id)
,CONSTRAINT cfr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
,CONSTRAINT cfr_court_fee_id_fk FOREIGN KEY (court_fee_id) REFERENCES court_fees (court_fee_id) 
,CONSTRAINT cfr_suspense_transaction_id_fk FOREIGN KEY (suspense_transaction_id) REFERENCES suspense_transactions (suspense_transaction_id)  
);

COMMENT ON COLUMN court_fees_received.court_fee_received_id IS 'Unique ID of this record';
COMMENT ON COLUMN court_fees_received.business_unit_id IS 'ID of the business unit that charged this court fee';
COMMENT ON COLUMN court_fees_received.court_fee_id IS 'ID of the court fee the payment is in regard of. Null for any amounts overpaid';
COMMENT ON COLUMN court_fees_received.overpayment IS 'If this amount is a fee overpayment';
COMMENT ON COLUMN court_fees_received.suspense_transaction_id IS 'ID of ths suspense item created for this payment';
COMMENT ON COLUMN court_fees_received.transferred_date IS 'Date transferred to HMCTS';
COMMENT ON COLUMN court_fees_received.number_of_items IS 'Number of instances of the fee covered by this payment. 0 of this is an overpayment';