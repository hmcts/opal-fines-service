/**
* OPAL Program
*
* MODULE      : control_totals.sql
*
* DESCRIPTION : Create the CONTROL_TOTALS table in the Fines model. 
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 04/06/2024    I Readman    1.0         PO-396 Create the CONTROL_TOTALS table in the Fines model
*
**/ 

CREATE TABLE control_totals
(
 control_total_id          bigint
,business_unit_id          smallint        not null 
,item_number               smallint        not null
,amount                    decimal(18,2)   not null
,associated_record_type    varchar(30)
,associated_record_id      varchar(30)
,ct_report_instance_id     bigint
,qe_report_instance_id     bigint
,CONSTRAINT control_totals_pk PRIMARY KEY (control_total_id) 
,CONSTRAINT ct_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id)
,CONSTRAINT ct_ct_report_instance_id_fk FOREIGN KEY (ct_report_instance_id) REFERENCES report_instances (report_instance_id)
,CONSTRAINT ct_qe_report_instance_id_fk FOREIGN KEY (qe_report_instance_id) REFERENCES report_instances (report_instance_id)
);

COMMENT ON COLUMN control_totals.control_total_id IS 'Unique ID of this record';
COMMENT ON COLUMN control_totals.business_unit_id IS 'ID of the relating business unit';
COMMENT ON COLUMN control_totals.item_number IS 'Control total item number';
COMMENT ON COLUMN control_totals.amount IS 'Amount of this item';
COMMENT ON COLUMN control_totals.associated_record_type IS 'Type of record identified by associated_record_id';
COMMENT ON COLUMN control_totals.associated_record_id IS 'ID or other reference/number of an associated record';
COMMENT ON COLUMN control_totals.ct_report_instance_id IS 'Report instance where this amount was reported on control totals';
COMMENT ON COLUMN control_totals.qe_report_instance_id IS 'Report instance where this amount was reported on quarter end';