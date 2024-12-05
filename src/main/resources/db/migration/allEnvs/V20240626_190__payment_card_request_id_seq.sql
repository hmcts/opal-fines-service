/**
* OPAL Program
*
* MODULE      : payment_card_request_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table PAYMENT_CARD_REQUESTS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-391 Create the sequence to be used to generate the Primary key for the table PAYMENT_CARD_REQUESTS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS payment_card_request_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY payment_card_requests.payment_card_request_id;