/**
* OPAL Program
*
* MODULE      : insert_defendant_transactions.sql
*
* DESCRIPTION : Inserts rows of data into the DEFENDANT_TRANSACTIONS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-154 Inserts rows of data into the DEFENDANT_TRANSACTIONS table
*
**/
INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                             
)
VALUES
(
 500000000
,500000000
,'2023-12-02'
,'1000001'
,'CONSOL'
,1000.50
,'NC'
,NULL  -- not null if payment method is CQ
,'Review of processing'
,'C'
,'2023-12-01' 
,NULL
,NULL
,NULL
,'REC000001'
,69.49   -- currently for only CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000001
,500000001
,'2023-12-03'
,'1000002'
,'ENABDC'
,900
,'CQ'
,'CQ00001'  -- not null if payment method is CQ
,'Amount determined again'
,'D'
,'2023-12-02' 
,NULL
,'Cheque'
,NULL
,'0457222'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000002
,500000002
,'2023-12-04'
,'1000003'
,'FCOST'
,500
,'CT'
,NULL  -- not null if payment method is CQ
,'Reviewd'
,'P'
,'2023-12-03' 
,200.00
,NULL
,'suspense_transaction'
,'PAR00001'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000003
,500000003
,'2023-12-05'
,'1000004'
,'FLAID'
,850.00
,'PO'
,NULL  -- not null if payment method is CQ
,'Evaluated'
,'R'
,'2023-12-04' 
,850.00
,'TRNOUT'
,'imposition'
,'IMP0001'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000004
,500000004
,'2023-12-06'
,'1000005'
,'FO'
,600.00
,'NC'
,NULL  -- not null if payment method is CQ
,'Turn up update'
,'X'
,'2023-12-05' 
,NULL
,'JCAM-A'
,'suspense_transaction'
,'QW1111A'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000005
,500000005
,'2023-12-07'
,'1000006'
,'NOTE'
,400.00
,'CQ'
,'TR00005'  -- not null if payment method is CQ
,'Done deal'
,'C'
,'2023-12-06' 
,100.00
,NULL
,'cheque'
,'CH00129'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000006
,500000006
,'2023-12-08'
,'1000007'
,'PAYMNT'
,2000.00
,'CT'
,NULL  -- not null if payment method is CQ
,'Publish to client'
,'C'
,'2023-12-07' 
,NULL
,NULL
,NULL
,'XA00008'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000007
,500000007
,'2023-12-09'
,'1000008'
,'TFO IN'
,1500.00
,'CT'
,NULL  -- not null if payment method is CQ
,'Change location'
,'C'
,'2023-12-08' 
,500
,NULL
,'TFO001'
,'TX00001'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000008
,500000008
,'2023-12-10'
,'1000009'
,'TTPAY'
,2100.00
,'PO'
,NULL  -- not null if payment method is CQ
,'Case reviewed'
,'P'
,'2023-12-09' 
,800
,NULL
,'imposition'
,'TT00012'
,NULL   -- currently for CONSOL
);

INSERT INTO defendant_transactions
(               
 defendant_transaction_id       
,defendant_account_id           
,posted_date                    
,posted_by                      
,transaction_type               
,transaction_amount             
,payment_method                 
,payment_reference              
,text                           
,status                         
,status_date                    
,status_amount                  
,write_off_code                 
,associated_record_type         
,associated_record_id           
,imposed_amount                               
)
VALUES
(
 500000009
,500000009
,'2023-12-11'
,'1000010'
,'RVWOFF'
,2300.00
,'CQ'
,'HO22201'  -- not null if payment method is CQ
,'Upgrade'
,'C'
,'2023-12-10' 
,NULL
,NULL
,'cheque'
,'PP0019'
,NULL   -- currently for CONSOL
);
