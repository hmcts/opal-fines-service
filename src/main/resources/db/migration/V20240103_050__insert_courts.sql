/**
* OPAL Program
*
* MODULE      : insert_courts.sql
*
* DESCRIPTION : Inserts rows of data into the COURTS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 03/01/2024    A Dennis    1.0         PO-147 Inserts one row of data into the COURTS table
*
**/
INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              -- PSA_CODE
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   -- PSA ID
  ,national_court_code     -- COURT_LOCATION_CODE   
  )
VALUES
  (
   500000000
  ,5000
  ,2812
  ,NULL
  ,'Kingston-upon-Thames Mags Court'
  ,NULL
  ,'Kingston upon Thames'
  ,'20 Cramer Road'
  ,'Kingston'
  ,NULL
  ,NULL
  ,NULL
  ,'KT1 5DR'
  ,2022
  ,'B01HO00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000001
  ,5001
  ,2014
  ,NULL
  ,'Reedley Family Court'
  ,NULL
  ,'The Court House'
  ,'Colne Road'
  ,'Burnley'
  ,NULL
  ,NULL
  ,NULL
  ,'BB10 2LJ'
  ,2216
  ,'B04KU00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000002
  ,5002
  ,2002
  ,NULL
  ,'Lancaster County Court'
  ,NULL
  ,'Lancaster Court'
  ,'34 Wayne Street'
  ,'Lancaster'
  ,NULL
  ,NULL
  ,NULL
  ,'LN3 9XC'
  ,2232
  ,'B04RX00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000003
  ,5003
  ,2917
  ,NULL
  ,'Aldridge Magistrates'' Court'
  ,NULL
  ,'Aldridge Court'
  ,'97 Bruno Street'
  ,'Liverpool'
  ,NULL
  ,NULL
  ,NULL
  ,'LI3 6YT'
  ,2651
  ,'B20NP00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000004
  ,5004
  ,1747
  ,NULL
  ,'City of Salford Magistrates'' Court'
  ,NULL
  ,'Salford Court'
  ,'12 Davies Street'
  ,'Salford'
  ,NULL
  ,NULL
  ,NULL
  ,'SL3 4VB'
  ,2078
  ,'B06LI00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000005
  ,5005
  ,5188
  ,NULL
  ,'Chester Youth Court'
  ,NULL
  ,'Chester Court'
  ,'78 Dobby Street'
  ,'Chester'
  ,NULL
  ,NULL
  ,NULL
  ,'CH4 9VW'
  ,1755
  ,'B07DM00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000006
  ,5006
  ,2269
  ,NULL
  ,'North Sefton Magistrates'' Court'
  ,NULL
  ,'Sefton Court'
  ,'31 Granger Street'
  ,'Sefton'
  ,NULL
  ,NULL
  ,NULL
  ,'NS2 4JU'
  ,2320
  ,'B05MC00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000007
  ,5007
  ,1940
  ,NULL
  ,'Grimsby Magistrates'' Court'
  ,NULL
  ,'Grimsby Court'
  ,'89 Molly Drive'
  ,'Grismby'
  ,NULL
  ,NULL
  ,NULL
  ,'GR3 8NH'
  ,2160
  ,'B16GB00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000008
  ,5008
  ,1966
  ,NULL
  ,'Medway Magistrates'' Court'
  ,NULL
  ,'Medway Court'
  ,'12 Baines Park'
  ,'Chatham'
  ,NULL
  ,NULL
  ,NULL
  ,'ME7 2NB'
  ,3119
  ,'B46DH00'
  );

INSERT INTO COURTS
  (
   court_id                
  ,business_unit_id        
  ,court_code              
  ,parent_court_id         
  ,name                    
  ,name_cy                 
  ,address_line_1          
  ,address_line_2          
  ,address_line_3          
  ,address_line_1_cy       
  ,address_line_2_cy       
  ,address_line_3_cy       
  ,postcode                
  ,local_justice_area_id   
  ,national_court_code        
  )
VALUES
  (
   500000009
  ,5009
  ,3348
  ,NULL
  ,'Cardiff Magistrates'' Court'
  ,'Llys Ynadon Caerdydd'
  ,'Cardiff Court'
  ,'11 High Street'
  ,'Cardiff'
  ,NULL
  ,NULL
  ,NULL
  ,'CF3 3JC'
  ,2831
  ,'B62DC00'
  );
