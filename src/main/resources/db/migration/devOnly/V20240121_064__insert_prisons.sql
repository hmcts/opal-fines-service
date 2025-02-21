/**
* OPAL Program
*
* MODULE      : insert_prosons.sql
*
* DESCRIPTION : Inserts rows of data into the PRISONS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-147 Inserts rows of data into the PRISONS table
*
**/
INSERT INTO prisons
(               
 prison_id             
,business_unit_id      
,prison_code           
,name                  
,address_line_1        
,address_line_2        
,address_line_3        
,postcode                     
)
VALUES
(
 500000000
,5000
,'WORM'
,'Wormwood Scrubs'
,'P.O. Box 757'
,'Du Cane Road'
,'London'
,'W12 0AE'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000001
,5000
,'BELM'
,'Belmarsh'
,'Western Way'
,'Thamesmead'
,'London'
,'SE28 0EB'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000002
,5009
,'READ'
,'Reading'
,'Forbury Road'
,'Reading'
,'Berkshire'
,'RG1 3HY'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000003
,5001
,'CARD'
,'Cardiff'
,'Knox Road'
,'Cardiff'
,NULL
,'CF24 0UG'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000004
,5007
,'HUMB'
,'HMP Humber'
,'Freton Road'
,'Everthorpe'
,'Humberside'
,'HM2 5HJ'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000005
,5003
,'LIVE'
,'HMP Liverpool'
,'Lanton Road'
,'Liverpool'
,'Merseyside'
,'LI4 6HG'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000006
,5008
,'ROCH'
,'HMP/YOI Rochester'
,'Medway Road'
,'Rochester'
,'Meadway'
,'ME8 2BN'
);

INSERT INTO prisons
(               
 prison_id         
,business_unit_id  
,prison_code       
,name              
,address_line_1    
,address_line_2    
,address_line_3    
,postcode          
)
VALUES
(
 500000007
,5004
,'MANC'
,'HMP/YOI Manchester'
,'Torridge Road'
,'Manchester'
,NULL
,'MA8 4VM'
);
