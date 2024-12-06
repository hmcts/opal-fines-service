/**
* OPAL Program
*
* MODULE      : insert_payments_in.sql
*
* DESCRIPTION : Inserts rows of data into the PAYMENTS_IN table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 21/01/2024    A Dennis    1.0         PO-154 Inserts rows of data into the PAYMENTS_IN table
*
**/
INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000000
,500000000
,497.25
,'2024-01-01'
,'NC'
,'S'
,'Partial'
,'suspense item'
,'R00000000001'
,'Robbie Franklin'
,'To determine outstanding payment'
,'Y'
,'Y'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000001
,500000001
,698.21
,'2024-01-02'
,'CQ'
,'F'
,'Full'
,'court fee'
,'R00000000002'
,'Daniel Akim'
,'Moved aahead'
,'N'
,'N'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000002
,500000002
,200
,'2024-01-03'
,'CT'
,'C'
,'Deposit'
,'miscellaneous account'
,'R00000000003'
,'Mohammed Chitoy'
,'Request suggested'
,'N'
,'Y'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000003
,500000003
,582.11
,'2024-01-04'
,'PO'
,'F'
,'Overpaid'
,'defendant account'
,'R00000000004'
,'Mazimov Ishtal'
,'Breakdown transactions'
,'Y'
,'N'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000004
,500000004
,1400.00
,'2024-01-05'
,'NC'
,'S'
,'Underpaid'
,'party'
,'R00000000005'
,'Toshiro Nagama'
,'Future applications'
,'N'
,'Y'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000005
,500000005
,9.00
,'2024-01-06'
,'CQ'
,'C'
,'Balanced'
,'suspense item'
,'R00000000006'
,'Marina Treshtomima'
,'Requests explained'
,'Y'
,'Y'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000006
,500000006
,0.99
,'2024-01-07'
,'CT'
,'F'
,'Surplus'
,'court fee'
,'R00000000007'
,'Rose Ellederly'
,'Minimal Token'
,'N'
,'N'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000007
,500000007
,19.99
,'2024-01-08'
,'PO'
,'S'
,'Underwritten'
,'miscellaneous account'
,'R00000000008'
,'Ade Sankara'
,'New information'
,'Y'
,'N'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000008
,500000008
,785
,'2024-01-09'
,'NC'
,'C'
,'Full'
,'defendant account'
,'R00000000009'
,'Anders Johnsen'
,'Old report'
,'N'
,'Y'
);

INSERT INTO payments_in
(               
 payment_in_id              
,till_id                    
,payment_amount             
,payment_date               
,payment_method             
,destination_type           
,allocation_type            
,associated_record_type     
,associated_record_id       
,third_party_payer_name     
,additional_information     
,receipt                    
,allocated                                   
)
VALUES
(
 500000009
,500000009
,945.07
,'2024-01-10'
,'CQ'
,'F'
,'Partial'
,'defendant account'
,'R0000000000a0'
,'Joel Martins'
,'Ceased information'
,'Y'
,'Y'
);
