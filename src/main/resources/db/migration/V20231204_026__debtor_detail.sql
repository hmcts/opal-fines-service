/**
* CGI OPAL Program
*
* MODULE      : debtor_detail.sql
*
* DESCRIPTION : Creates the DEBTOR_DETAIL table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 04/12/2023    A Dennis    1.0         PO-127 Creates the DEBTOR_DETAIL table for the Fines model
*
**/
CREATE TABLE debtor_detail 
(
 party_id                   bigint  not null
,telephone_home             varchar(35)
,telephone_business         varchar(35)
,telephone_mobile           varchar(35)
,email_1                    varchar(80)
,email_2                    varchar(80)
,vehicle_make               varchar(30)
,vehicle_registration       varchar(20)
,employer_name              varchar(50)
,employer_address_line_1    varchar(35)
,employer_address_line_2    varchar(35)
,employer_address_line_3    varchar(35)
,employer_address_line_4    varchar(35)
,employer_address_line_5    varchar(35)
,employer_postcode          varchar(10)
,employer_reference         varchar(35)
,employer_telephone         varchar(35)
,employer_email             varchar(80)
,document_language          varchar(2)
,document_language_date     timestamp
,hearing_language           varchar(2)
,hearing_language_date      timestamp
,CONSTRAINT debtor_detail_pk PRIMARY KEY 
 (
   party_id	
 ) 
);

ALTER TABLE debtor_detail
ADD CONSTRAINT dd_party_id_fk FOREIGN KEY
(
  party_id 
)
REFERENCES parties
(
  party_id 
);

COMMENT ON COLUMN debtor_detail.party_id IS 'Unique ID of this record';
COMMENT ON COLUMN debtor_detail.telephone_home IS 'Home telephone number';
COMMENT ON COLUMN debtor_detail.telephone_business IS 'Business telephone number';
COMMENT ON COLUMN debtor_detail.telephone_mobile IS 'Mobile telephone number';
COMMENT ON COLUMN debtor_detail.email_1 IS 'Primary email address';
COMMENT ON COLUMN debtor_detail.email_2 IS 'Secondary email address';
COMMENT ON COLUMN debtor_detail.vehicle_make IS 'Debtor asset vehicle make';
COMMENT ON COLUMN debtor_detail.vehicle_registration IS 'Debtor asset vehicle registration';
COMMENT ON COLUMN debtor_detail.employer_name IS 'Employer name';
COMMENT ON COLUMN debtor_detail.employer_address_line_1 IS 'Employer address line 1';
COMMENT ON COLUMN debtor_detail.employer_address_line_2 IS 'Employer address line 2';
COMMENT ON COLUMN debtor_detail.employer_address_line_3 IS 'Employer address line 3';
COMMENT ON COLUMN debtor_detail.employer_address_line_4 IS 'Employer address line 4';
COMMENT ON COLUMN debtor_detail.employer_address_line_5 IS 'Employer address line 5';
COMMENT ON COLUMN debtor_detail.employer_postcode IS 'Employer postcode';
COMMENT ON COLUMN debtor_detail.employer_reference IS 'Employer reference number';
COMMENT ON COLUMN debtor_detail.employer_telephone IS 'Employer telephone number';
COMMENT ON COLUMN debtor_detail.employer_email IS 'Employer email address';
COMMENT ON COLUMN debtor_detail.document_language IS 'Document language preference (CY or EN)';
COMMENT ON COLUMN debtor_detail.document_language_date IS 'Document language preference effective date';
COMMENT ON COLUMN debtor_detail.hearing_language IS 'Hearing language preference (CY or EN)';
COMMENT ON COLUMN debtor_detail.hearing_language_date IS 'Hearing language preference effective date';
