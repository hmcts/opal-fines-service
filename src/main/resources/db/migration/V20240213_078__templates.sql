/**
* CGI OPAL Program
*
* MODULE      : templates.sql
*
* DESCRIPTION : Creates the TEMPLATES table for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 13/02/2024    A Dennis    1.0         PO-177 Creates the TEMPLATES table for the Fines model
*
**/
CREATE TABLE templates 
(
 template_id       bigint          not null
,template_name     varchar(100)  
,CONSTRAINT templates_pk PRIMARY KEY 
 (
   template_id	
 ) 
);

COMMENT ON COLUMN templates.template_id IS 'Unique ID of this record';
COMMENT ON COLUMN templates.template_name IS 'The template name or description';
