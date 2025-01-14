/**
* OPAL Program
*
* MODULE      : document_instances.sql
*
* DESCRIPTION : Recreate the DOCUMENT_INSTANCES table after reference data work done by Capita
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------
* 14/01/2025    A Dennis    1.0         PO-970  Recreate the DOCUMENT_INSTANCES table after reference data work done by Capita
*
**/

CREATE TABLE document_instances(
    document_instance_id bigint NOT NULL,
    document_id varchar(10) NOT NULL,
    business_unit_id smallint NOT NULL,
    generated_date timestamp NOT NULL,
    generated_by varchar(20) NOT NULL,
    associated_record_type varchar(30) NOT NULL,
    associated_record_id varchar(30) NOT NULL,
    status varchar(10) NOT NULL,
    printed_date timestamp,
    document_content xml NOT NULL);

ALTER TABLE document_instances
    ADD CONSTRAINT document_instances_pk 
        PRIMARY KEY (document_instance_id),
    ADD CONSTRAINT di_document_id_fk
        FOREIGN KEY (document_id)
        REFERENCES documents (document_id),
    ADD CONSTRAINT di_business_unit_fk 
        FOREIGN KEY (business_unit_id)
        REFERENCES business_units (business_unit_id);

CREATE INDEX IF NOT EXISTS di_bu_document_status_date_idx ON document_instances(business_unit_id, document_id, status, generated_date);

COMMENT ON COLUMN document_instances.document_instance_id IS 'Unique ID for this record';

COMMENT ON COLUMN document_instances.document_id IS 'ID of the report being generated';

COMMENT ON COLUMN document_instances.business_unit_id IS 'ID of the business unit this report instance was generated for';

COMMENT ON COLUMN document_instances.generated_date IS 'The date the document was generated';

COMMENT ON COLUMN document_instances.generated_by IS 'ID of the user that generated this instance of the document';

COMMENT ON COLUMN document_instances.document_content IS 'The structured document content';
