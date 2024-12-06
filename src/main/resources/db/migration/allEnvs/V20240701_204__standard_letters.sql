/**
* OPAL Program
*
* MODULE      : standard_letters.sql
*
* DESCRIPTION : Create the STANDARD_LETTERS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-398 Create the STANDARD_LETTERS table in the Fines model
*
**/ 

CREATE TABLE standard_letters
(
 standard_letter_id       bigint
,business_unit_id         smallint        not null 
,standard_letter_code     varchar(10)     not null
,standard_letter_name     varchar(50)     not null
,associated_record_type   varchar(30)     not null
,user_entries             json
,document_body            text            not null
,CONSTRAINT standard_letters_pk PRIMARY KEY (standard_letter_id)
,CONSTRAINT ct_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
);

COMMENT ON COLUMN standard_letters.standard_letter_id IS 'Unique ID of this record';
COMMENT ON COLUMN standard_letters.business_unit_id IS 'ID of the business unit that owns this record';
COMMENT ON COLUMN standard_letters.standard_letter_code IS 'Standard letter code unique within the business unit';
COMMENT ON COLUMN standard_letters.standard_letter_name IS 'Unique name for this Standard Letter';
COMMENT ON COLUMN standard_letters.associated_record_type IS 'The type of record for which this letter is generated (defendant_account or creditor_account)';
COMMENT ON COLUMN standard_letters.user_entries IS 'Parameters required to be entered by the user when generating an instance of this letter';
COMMENT ON COLUMN standard_letters.document_body IS 'The document body including user parameter and formatting tags';