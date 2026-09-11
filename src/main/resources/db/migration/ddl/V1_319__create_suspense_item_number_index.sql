/**
* OPAL Program
*
* MODULE      : create_suspense_item_number_index.sql
*
* DESCRIPTION : Create SUSPENSE_ITEM_NUMBER_INDEX table and related components.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 11/09/2026    C Cho       1.0         PO-3391 Create SUSPENSE_ITEM_NUMBER_INDEX table and related components.
*
**/

CREATE SEQUENCE suspense_item_number_index_id_seq
    START WITH 100000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    NO CYCLE
    CACHE 1;

CREATE TABLE suspense_item_number_index (
    suspense_item_number_index_id BIGINT DEFAULT nextval('suspense_item_number_index_id_seq') NOT NULL,
    suspense_item_number BIGINT NOT NULL,
    business_unit_id SMALLINT NOT NULL,
    CONSTRAINT sini_pk PRIMARY KEY (suspense_item_number_index_id),
    CONSTRAINT sini_business_unit_id_suspense_item_number_uk UNIQUE (business_unit_id, suspense_item_number)
);

COMMENT ON COLUMN suspense_item_number_index.suspense_item_number_index_id IS 'Unique ID of this record';
COMMENT ON COLUMN suspense_item_number_index.suspense_item_number IS 'Suspense item number unique within the Business Unit. Programatically generated each time by the calling process starting from 100,000, so it does not clash with numbers from Legacy GoB, and increments by 1';
COMMENT ON COLUMN suspense_item_number_index.business_unit_id IS 'ID of the relating Business Unit';

ALTER SEQUENCE suspense_item_number_index_id_seq
    OWNED BY suspense_item_number_index.suspense_item_number_index_id;

ALTER TABLE suspense_item_number_index
    ADD CONSTRAINT sini_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

CREATE INDEX sini_business_unit_id_idx
    ON suspense_item_number_index (business_unit_id);
