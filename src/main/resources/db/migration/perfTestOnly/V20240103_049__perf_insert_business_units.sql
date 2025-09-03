/**
* OPAL Program
*
* MODULE      : insert_business_units.sql
*
* DESCRIPTION : Inserts rows of data into the BUSINESS_UNITS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 03/01/2024    A Dennis    1.0         PO-147 Inserts one row of data into the BUSINESS_UNITS table
*
**/
INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5000
  ,'Greater London'
  ,'01'
  ,'7'
  ,'GL'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5001
  ,'Cumbria'
  ,'03'
  ,'7'
  ,'CU'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5002
  ,'Lancashire'
  ,'04'
  ,'7'
  ,'LA'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5003
  ,'Merseyside'
  ,'05'
  ,'7'
  ,'ME'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5004
  ,'Greater Manchester'
  ,'06'
  ,'7'
  ,'GM'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5005
  ,'Cheshire'
  ,'07'
  ,'7'
  ,'CH'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5006
  ,'North Sefton'
  ,'0086'
  ,'5'
  ,'NS'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5007
  ,'Humberside Management Unit 1'
  ,'MU01'
  ,'4'
  ,'HM'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5008
  ,'West Kent'
  ,'0076'
  ,'5'
  ,'HM'
  ,NULL
  );

INSERT INTO BUSINESS_UNITS
  (
   business_unit_id	         
  ,business_unit_name	       
  ,business_unit_code        
  ,business_unit_type                    
  ,account_number_prefix     
  ,parent_business_unit_id    
  )
VALUES
  (
   5009
  ,'DMU 1'
  ,'DMU1'
  ,'6'
  ,'D1'
  ,NULL
  );
