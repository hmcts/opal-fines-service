/**
*
* OPAL Program
*
* MODULE      : create_write_off_codes.sql
*
* DESCRIPTION : Create WRITE_OFF_CODES table and supporting enum for Admin Write Off
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 09/06/2026    P Brumby    1.0         PO-3453 Create WRITE_OFF_CODES table and supporting enum for Admin Write Off
*
**/

CREATE TYPE t_write_off_category_enum AS ENUM (
    'Admin',
    'Judicial',
    'System'
);

CREATE TABLE write_off_codes (
    write_off_code_id VARCHAR(10) NOT NULL,
    reason_description VARCHAR(60),
    category t_write_off_category_enum,
    write_off_code_value smallint,
    CONSTRAINT write_off_codes_upper_cc CHECK (write_off_code_id = UPPER(write_off_code_id))
);

COMMENT ON COLUMN write_off_codes.write_off_code_id IS 'The unique write off code. Always stored in upper case.';
COMMENT ON COLUMN write_off_codes.reason_description IS 'The write off code reason description, used as display name where known.';
COMMENT ON COLUMN write_off_codes.category IS 'Category of write off code (Admin, Judicial, System).';
COMMENT ON COLUMN write_off_codes.write_off_code_value IS 'Write off code associated opal value used to resolve control_totals.item_number.';

ALTER TABLE ONLY write_off_codes
    ADD CONSTRAINT write_off_codes_pk PRIMARY KEY (write_off_code_id);
