/**
* CGI OPAL Program
*
* MODULE      : template_mappings.sql
*
* DESCRIPTION : Creates the TEMPLATE_MAPPINGSS table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 13/02/2024    A Dennis    1.0         PO-177 Creates the TEMPLATE_MAPPINGS table for the Fines model
*
**/
CREATE TABLE template_mappings 
(
 template_id                 bigint          not null
,application_function_id     bigint          not null
,CONSTRAINT template_mappings_pk PRIMARY KEY 
 (
   template_id, application_function_id	
 ) 
);

ALTER TABLE template_mappings
ADD CONSTRAINT tm_template_id_fk FOREIGN KEY
(
  template_id 
)
REFERENCES templates
(
  template_id 
);

ALTER TABLE template_mappings
ADD CONSTRAINT tm_application_function_id_fk FOREIGN KEY
(
  application_function_id 
)
REFERENCES application_functions
(
  application_function_id 
);

COMMENT ON COLUMN template_mappings.template_id IS 'ID of the template';
COMMENT ON COLUMN template_mappings.application_function_id IS 'ID of the application function';
