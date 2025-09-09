/**
* OPAL Program
*
* MODULE      : insert_templates.sql
*
* DESCRIPTION : Inserts rows of data into the TEMPLATES table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 19/02/2024    A Dennis    1.0         PO-179 Inserts rows of data into the TEMPLATES table
*
**/
INSERT INTO templates
  (
   template_id	         
  ,template_name        
  )
VALUES
  (
   500000000
  ,'Enforcement'
  );

INSERT INTO templates
  (
   template_id	         
  ,template_name        
  )
VALUES
  (
   500000001
  ,'Cash Team'
  );

INSERT INTO templates
  (
   template_id	         
  ,template_name        
  )
VALUES
  (
   500000002
  ,'Maintenance'
  );

INSERT INTO templates
  (
   template_id	         
  ,template_name        
  )
VALUES
  (
   500000003
  ,'Read Only'
  );
