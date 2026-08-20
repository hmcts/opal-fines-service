/**
* OPAL Program
*
* MODULE      : create_alternate_payment_references_for_ei2.sql
*
* DESCRIPTION : Create ALTERNATE_PAYMENT_REFERENCES table and related components for EI2
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------------
* 07/08/2026    TMc            1.0         PO-5770 - Create ALTERNATE_PAYMENT_REFERENCES table and related components for EI2
*
**/

CREATE TYPE t_apr_relationship_enum AS ENUM ('APR', 'CONSOLIDATED', 'AMALGAMATED');

CREATE TYPE t_apr_category_enum AS ENUM ('APR', 'ACC');

CREATE SEQUENCE alternate_payment_reference_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    NO CYCLE
    CACHE 1;

CREATE TABLE alternate_payment_references (
    alternate_payment_reference_id BIGINT DEFAULT nextval('alternate_payment_reference_id_seq') NOT NULL,
    defendant_account_id BIGINT NOT NULL,
    relationship t_apr_relationship_enum NOT NULL,
    category t_apr_category_enum NOT NULL,
    business_unit_code VARCHAR(4) NOT NULL,
    apr_text VARCHAR NOT NULL,
    created_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_datetime TIMESTAMP NOT NULL,
    CONSTRAINT alternate_payment_reference_pk PRIMARY KEY (alternate_payment_reference_id)
);

COMMENT ON COLUMN alternate_payment_references.alternate_payment_reference_id IS 'Unique ID (PK) of this record';
COMMENT ON COLUMN alternate_payment_references.defendant_account_id IS 'The ID of the Defendant account this APR is related to.';
COMMENT ON COLUMN alternate_payment_references.relationship IS 'Nature of relationship between APR and Defendant account.';
COMMENT ON COLUMN alternate_payment_references.category IS 'Whether the APR text value is an APR or a Fines/Maintenance Account.';
COMMENT ON COLUMN alternate_payment_references.business_unit_code IS 'The business unit at time of creation.';
COMMENT ON COLUMN alternate_payment_references.apr_text IS 'The APR text value.';
COMMENT ON COLUMN alternate_payment_references.created_datetime IS 'The timestamp the APR record was created.';
COMMENT ON COLUMN alternate_payment_references.updated_datetime IS 'The timestamp whenever the APR is encountered.';

ALTER SEQUENCE alternate_payment_reference_id_seq 
    OWNED BY alternate_payment_references.alternate_payment_reference_id;

ALTER TABLE alternate_payment_references
    ADD CONSTRAINT apr_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES defendant_accounts(defendant_account_id);

CREATE INDEX apr_defendant_account_id_idx
    ON alternate_payment_references (defendant_account_id);
