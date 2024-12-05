/**
*
* OPAL Program
*
* MODULE      : report_instances.sql
*
* DESCRIPTION : Create the table REPORT_INSTANCES in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-401 Create the table REPORT_INSTANCES in the Fines model
*
**/  
     
CREATE TABLE report_instances
(
 report_instance_id      bigint
,report_id               bigint          not null
,business_unit_id        smallint        not null
,audit_sequence          bigint          not null
,generated_date          timestamp       not null
,generated_by            varchar(20)     not null
,report_parameters       json            not null
,content                 xml             not null
,CONSTRAINT report_instances_pk PRIMARY KEY (report_instance_id)
,CONSTRAINT ri_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
);

COMMENT ON COLUMN report_instances.report_instance_id IS 'Unique ID for this record';
COMMENT ON COLUMN report_instances.report_id IS 'ID of the report being generated';
COMMENT ON COLUMN report_instances.business_unit_id IS 'ID of the business unit this report instance was generated for';
COMMENT ON COLUMN report_instances.audit_sequence IS 'The sequence_number of this report';
COMMENT ON COLUMN report_instances.generated_date IS 'The date the report was generated';
COMMENT ON COLUMN report_instances.generated_by IS 'ID of the user that generated this instance of the report';
COMMENT ON COLUMN report_instances.report_parameters IS 'The parameters used to generate the report';
COMMENT ON COLUMN report_instances.content IS 'The structured report content'; 