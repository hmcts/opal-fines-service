/**
* OPAL Program
*
* MODULE      : bacs_payment_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table BACS_PAYMENTS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-395 Create the sequence to be used to generate the Primary key for the table BACS_PAYMENTS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS bacs_payment_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY bacs_payments.bacs_payment_id;