/**
* OPAL Program
*
* MODULE      : insert_mis_debtors.sql
*
* DESCRIPTION : Inserts rows of data into the MIS_DEBTORS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-154 Inserts rows of data into the MIS_DEBTORS table
*
**/
INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                
)
VALUES
(
 500000000
,5000
,'Joao Mendes Phillpe'
,'J'
,'B'
,'A0000000001'
,'2024-01-01'
,'B'
,NULL
,679.89
,'2025-02-01'
,24
,'2023-12-01'
,'REM'
,21
,1000
,0
,1000
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000001
,5001
,'Kevin Ross'
,'J'
,'B'
,'A0000000002'
,'2024-01-02'
,'B'
,NULL
,592
,'2025-02-02'
,25
,'2023-12-02'
,'ENF'
,NULL
,1100
,0
,1100
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000002
,5002
,'Yoti Staton'
,'J'
,'B'
,'A0000000003'
,'2024-01-03'
,'B'
,NULL
,780
,'2025-02-03'
,26
,'2023-12-03'
,'ENF'
,NULL
,1200
,0
,1200
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000003
,5003
,'Greg Tomasz'
,'J'
,'B'
,'A0000000004'
,'2024-01-04'
,'B'
,NULL
,999.00
,'2025-02-04'
,17
,'2023-12-04'
,'ACT'
,58.00
,1300
,0
,1300
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000004
,5004
,'Mandy Staff'
,'J'
,'B'
,'A0000000005'
,'2024-01-05'
,'I'
,30
,200
,'2025-02-05'
,15
,'2023-12-06'
,'ACT'
,60.00
,1400
,70
,1390
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000005
,5005
,'Mahmoud Sharata'
,'J'
,'B'
,'A0000000006'
,'2024-01-06'
,'I'
,40
,50.00
,'2025-02-06'
,13
,'2023-12-07'
,'ENT'
,80.00
,1000
,0
,1000
);

INSERT INTO mis_debtors
(               
 mis_debtor_id          
,business_unit_id       
,debtor_name            
,account_category       
,arrears_category       
,account_number         
,account_start_date     
,terms_type             
,instalment_amount      
,lump_sum               
,terms_date             
,days_in_jail           
,date_last_movement     
,last_enforcement       
,arrears                
,amount_imposed         
,amount_paid            
,amount_outstanding                                      
)
VALUES
(
 500000006
,5006
,'Freya Dido'
,'J'
,'B'
,'A0000000007'
,'2024-01-07'
,'I'
,33
,75.00
,'2025-02-07'
,12
,'2023-12-08'
,'ENT'
,NULL
,2000
,0
,2000
);