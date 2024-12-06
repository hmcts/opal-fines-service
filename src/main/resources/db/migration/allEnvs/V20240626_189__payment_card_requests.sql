/**
* OPAL Program
*
* MODULE      : payment_card_requests.sql
*
* DESCRIPTION : Create the PAYMENT_CARD_REQUESTS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-391 Create the PAYMENT_CARD_REQUESTS table in the Fines model
*
**/ 

CREATE TABLE payment_card_requests 
(
 payment_card_request_id       bigint
,interface_file_id             bigint          not null 
,business_unit_id              bigint          not null
,account_number                varchar(9)      not null 
,account_id                    bigint          not null 
,name                          varchar(19)     not null 
,title_inits                   varchar(20)     not null
,address1                      varchar(30)     not null
,address2                      varchar(30)     not null
,address3                      varchar(30)     not null
,postcode                      varchar(8)      not null
,name_on_card                  varchar(27)     not null
,CONSTRAINT payment_card_requests_pk PRIMARY KEY (payment_card_request_id)
,CONSTRAINT pcr_interface_file_id_fk FOREIGN KEY (interface_file_id) REFERENCES interface_files (interface_file_id)
,CONSTRAINT pcr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id) 
);

COMMENT ON COLUMN payment_card_requests.payment_card_request_id IS 'Unique ID of this record';
COMMENT ON COLUMN payment_card_requests.interface_file_id IS 'The ID of the interface file thie request was written to';
COMMENT ON COLUMN payment_card_requests.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN payment_card_requests.account_number IS 'Account number to be displayed on the card';
COMMENT ON COLUMN payment_card_requests.account_id IS 'ID of the account the request is for';
COMMENT ON COLUMN payment_card_requests.name IS 'Cardholder''s name';
COMMENT ON COLUMN payment_card_requests.title_inits IS 'Cardholder''s title and initials of name';
COMMENT ON COLUMN payment_card_requests.address1 IS 'Cardholder''s address line 1';
COMMENT ON COLUMN payment_card_requests.address2 IS 'Cardholder''s address line 2';
COMMENT ON COLUMN payment_card_requests.address3 IS 'Cardholder''s address line 3';
COMMENT ON COLUMN payment_card_requests.postcode IS 'Cardholder''s postcode';
COMMENT ON COLUMN payment_card_requests.name_on_card IS 'Name to be displayed on card';