/**
* OPAL Program
*
* MODULE      : insert_template_mappings.sql
*
* DESCRIPTION : Inserts rows of data into the TEMPLATE_MAPPINGS table for Manual Account Creation. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 25/05/2024    A Dennis    1.0         PO-380 Inserts rows of data into the TEMPLATE_MAPPINGS table for Manual Account Creation
*
**/
INSERT INTO template_mappings
  (
   template_id	         
  ,application_function_id        
  )
VALUES
  (
   500000000
  ,35
  );

INSERT INTO template_mappings
  (
   template_id	         
  ,application_function_id        
  )
VALUES
  (
   500000002
  ,35
  );

INSERT INTO template_mappings
  (
   template_id	         
  ,application_function_id        
  )
VALUES
  (
   500000002
  ,54
  );
