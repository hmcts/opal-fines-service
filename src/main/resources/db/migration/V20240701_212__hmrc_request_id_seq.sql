/**
* OPAL Program
*
* MODULE      : hmrc_request_id_seq.sql
*
* DESCRIPTION : Create the sequence to be used to generate the Primary key for the table HMRC_REQUESTS. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    --------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-402 Create the sequence to be used to generate the Primary key for the table HMRC_REQUESTS
*
**/ 

CREATE SEQUENCE IF NOT EXISTS hmrc_request_id_seq INCREMENT 1 MINVALUE 1 NO MAXVALUE START WITH 1 CACHE 20 OWNED BY hmrc_requests.hmrc_request_id;  