/**
* CGI OPAL Program
*
* MODULE      : suspense_items.sql
*
* DESCRIPTION : Creates the SUSPENSE_ITEMS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 03/04/2024    A Dennis    1.0         PO-200 Creates the SUSPENSE_ITEMS table for the Fines model
*
**/
CREATE TABLE suspense_items 
(
 suspense_item_id       bigint          not null
,suspense_account_id    bigint          not null
,suspense_item_number   smallint        not null
,suspense_item_type     varchar(2)      not null
,created_date           timestamp       not null
,payment_method         varchar(2)
,court_fee_id           bigint 
,CONSTRAINT suspense_items_pk PRIMARY KEY 
 (
  suspense_item_id	
 )  
);

ALTER TABLE suspense_items
ADD CONSTRAINT si_suspense_account_id_fk FOREIGN KEY
(
  suspense_account_id 
)
REFERENCES suspense_accounts
(
  suspense_account_id 
);

ALTER TABLE suspense_items
ADD CONSTRAINT si_court_fee_id_fk FOREIGN KEY
(
  court_fee_id 
)
REFERENCES court_fees
(
  court_fee_id 
);

COMMENT ON COLUMN suspense_items.suspense_item_id IS 'Unique ID of this record';
COMMENT ON COLUMN suspense_items.suspense_account_id IS 'Suspense account this item belongs to';
COMMENT ON COLUMN suspense_items.suspense_item_number IS 'Suspense item number unique within the business unit';
COMMENT ON COLUMN suspense_items.suspense_item_type IS 'Type of this suspense item';
COMMENT ON COLUMN suspense_items.created_date IS 'Date the suspense item was created';
COMMENT ON COLUMN suspense_items.payment_method IS 'The method of payment';
COMMENT ON COLUMN suspense_items.court_fee_id IS 'The associated court fee code id applicable';
