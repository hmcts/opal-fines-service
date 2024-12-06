/**
* OPAL Program
*
* MODULE      : court_fee_received_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table COURT_FEES_RECEIVED. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-392 Create the sequence to be used to generate the Primary key for the table COURT_FEES_RECEIVED
*
**/ 

CREATE SEQUENCE IF NOT EXISTS court_fee_received_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY court_fees_received.court_fee_received_id;
