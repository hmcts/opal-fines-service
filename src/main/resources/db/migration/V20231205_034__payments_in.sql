/**
* CGI OPAL Program
*
* MODULE      : payments_in.sql
*
* DESCRIPTION : Creates the PAYMENTS_IN table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 05/12/2023    A Dennis    1.0         PO-127 Creates the PAYMENTS_IN table for the Fines model
*
**/
CREATE TABLE payments_in 
(
 payment_in_id              bigint         not null
,till_id                    smallint       not null
,payment_amount             decimal(18,2)  not null
,payment_date               timestamp      not null
,payment_method             varchar(2)     not null
,destination_type           varchar(1)     not null
,allocation_type            varchar(20)
,associated_record_type     varchar(30)
,associated_record_id       varchar(30)
,third_party_payer_name     varchar(50)
,additional_information     varchar(500)
,receipt                    boolean
,allocated                  boolean
,CONSTRAINT payments_in_pk PRIMARY KEY 
 (
   payment_in_id	
 ) 
);

ALTER TABLE payments_in
ADD CONSTRAINT pi_till_id_fk FOREIGN KEY
(
  till_id 
)
REFERENCES tills
(
  till_id 
);

COMMENT ON COLUMN payments_in.payment_in_id IS 'Unique ID of this record';
COMMENT ON COLUMN payments_in.till_id IS 'ID of the relating till to which this till belongs';
COMMENT ON COLUMN payments_in.payment_amount IS 'Amount paid';
COMMENT ON COLUMN payments_in.payment_date IS 'Date payment received';
COMMENT ON COLUMN payments_in.payment_method IS 'Payment method';
COMMENT ON COLUMN payments_in.destination_type IS 'Allocation destination: F (fines), S (Suspense), C (Court Fee)';
COMMENT ON COLUMN payments_in.allocation_type IS 'Specific types for each allocation where an initial payment amount is split, for example, if an amount is overpaid.';
COMMENT ON COLUMN payments_in.associated_record_type IS 'Type of record identified by associated_record_id. This could be a suspense item, court fee, miscellaneous account, defendant account or party';
COMMENT ON COLUMN payments_in.associated_record_id IS 'ID or other reference/number of an associated record';
COMMENT ON COLUMN payments_in.third_party_payer_name IS 'Name of payer if a third party';
COMMENT ON COLUMN payments_in.additional_information IS 'Additional information stored against the payment';
COMMENT ON COLUMN payments_in.receipt IS 'If a receipt was requested';
COMMENT ON COLUMN payments_in.allocated IS 'If this payment has been allocation';
