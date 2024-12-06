/**
*
* OPAL Program
*
* MODULE      : report_entries.sql
*
* DESCRIPTION : Create the table REPORT_ENTRIES in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-403 Create the table REPORT_ENTRIES in the Fines model
*
**/    
     
CREATE TABLE report_entries
(
 report_entry_id          bigint
,business_unit_id         smallint    
,report_id                bigint   
,entry_timestamp          timestamp    
,reported_timestamp       timestamp 
,associated_record_type   varchar(30)
,associated_record_id     varchar(30)
,CONSTRAINT report_entries_pk PRIMARY KEY (report_entry_id)
,CONSTRAINT re_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
);

COMMENT ON COLUMN report_entries.report_entry_id IS 'Unique ID for this record'; 
COMMENT ON COLUMN report_entries.business_unit_id IS 'ID of the business unit';
COMMENT ON COLUMN report_entries.report_id IS 'The report that this record should be included on';
COMMENT ON COLUMN report_entries.entry_timestamp IS 'Timestamp when this entry was created';
COMMENT ON COLUMN report_entries.reported_timestamp IS 'Timestamp when this entry was added to a report';
COMMENT ON COLUMN report_entries.associated_record_type IS 'Type of record identified by associated_record_id';
COMMENT ON COLUMN report_entries.associated_record_id IS 'ID of the associated record';