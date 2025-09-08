/**
* CGI OPAL Program
*
* MODULE      : modify_document_instances.sql
*
* DESCRIPTION : Change document_instances.business_unit_id a foreign key to BUSINESS_UNITS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------
* 22/03/2024    A Dennis    1.0         PO-248 Change document_instances.business_unit_id a foreign key to BUSINESS_UNITS table for the Fines model
*
**/

ALTER TABLE document_instances
ADD CONSTRAINT di_business_unit_id_fk FOREIGN KEY
(
  business_unit_id 
)
REFERENCES business_units
(
  business_unit_id 
);
