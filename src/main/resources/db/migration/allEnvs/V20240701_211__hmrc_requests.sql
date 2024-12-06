/**
*
* OPAL Program
*
* MODULE      : hmrc_requests.sql
*
* DESCRIPTION : Create the table HMRC_REQUESTS in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-402 Create the table HMRC_REQUESTS in the Fines model
*
**/        

CREATE TABLE hmrc_requests
(
 hmrc_request_id        bigint
,business_unit_id       smallint        not null
,uuid                   varchar(36)     not null
,requested_date         timestamp       not null
,requested_by           bigint          not null
,status                 varchar(20)     not null
,account_id             bigint          not null
,forename               varchar(50)     not null
,surname                varchar(50)     not null
,ni_number              varchar(12)     not null
,dob                    timestamp       not null
,last_enforcement       varchar(24)
,response_date          timestamp
,response_data          text
,qa_report_data         text
,CONSTRAINT hmrc_requests_pk PRIMARY KEY (hmrc_request_id) 
,CONSTRAINT hr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
);

COMMENT ON COLUMN hmrc_requests.hmrc_request_id IS 'Unique ID of this record';
COMMENT ON COLUMN hmrc_requests.business_unit_id IS 'ID of the business unit this account type belongs to';
COMMENT ON COLUMN hmrc_requests.uuid IS 'Unique ID of the request message';
COMMENT ON COLUMN hmrc_requests.requested_date IS 'Date the request was sent';
COMMENT ON COLUMN hmrc_requests.requested_by IS 'Requested By';
COMMENT ON COLUMN hmrc_requests.status IS 'Request status';
COMMENT ON COLUMN hmrc_requests.account_id IS 'ID of the account the request is for';
COMMENT ON COLUMN hmrc_requests.forename IS 'Debtor''s forename';
COMMENT ON COLUMN hmrc_requests.surname IS 'Debtor''s surname';
COMMENT ON COLUMN hmrc_requests.ni_number IS 'Debtor''s national insurance number';
COMMENT ON COLUMN hmrc_requests.dob IS 'Debtor''s date of birth';
COMMENT ON COLUMN hmrc_requests.last_enforcement IS 'Account last enforcement action at the time of the request';
COMMENT ON COLUMN hmrc_requests.response_date IS 'Date the response was received';
COMMENT ON COLUMN hmrc_requests.response_data IS 'Response data';
COMMENT ON COLUMN hmrc_requests.qa_report_data IS 'Additional data in the response for output on the QA report'; 