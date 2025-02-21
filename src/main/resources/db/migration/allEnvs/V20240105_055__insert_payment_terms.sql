/**
* OPAL Program
*
* MODULE      : insert_payment_terms.sql
*
* DESCRIPTION : Inserts rows of data into the PAYMENT_TERMS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/01/2024    A Dennis    1.0         PO-147 Inserts one row of data into the PAYMENT_TERMS table
*
**/
INSERT INTO payment_terms
(
  payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000000
,500000000
,'2023-11-03 16:05:10'
,'01000000A'
,'B'
,'2025-10-12 00:00:00'
,NULL
,NULL
,NULL
,120
,'N'
,700.58
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000001
,500000001
,'2022-10-04 15:05:10'
,'02000000B'
,'B'
,'2025-12-14 00:00:00'
,NULL
,NULL
,NULL
,180
,'N'
,800.58
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000002
,500000002
,'2022-09-03 15:05:10'
,'03000000C'
,'B'
,'2025-10-13 00:00:00'
,NULL
,NULL
,NULL
,180
,'N'
,1000.58
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000003
,500000003
,'2022-09-03 15:05:10'
,'04000000D'
,'B'
,'2025-09-13 00:00:00'
,NULL
,NULL
,NULL
,200
,'N'
,1100.58
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000004
,500000004
,'2022-10-04 15:05:10'
,'05000000E'
,'I'
,'2024-10-04 00:00:00'
,'M'
,50
,100
,200
,'N'
,1300
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000005
,500000005
,'2022-11-05 11:05:10'
,'06000000F'
,'I'
,'2024-11-05 00:00:00'
,'M'
,116.66
,0
,200
,'N'
,1400
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000006
,500000006
,'2022-12-06 09:06:00'
,'07000000G'
,'I'
,'2024-12-06 00:00:00'
,'M'
,62.50
,0
,90
,'N'
,1500
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000007
,500000007
,'2022-08-07 09:06:00'
,'08000000H'
,'I'
,'2024-08-07 00:00:00'
,'M'
,54.16
,300
,80
,'N'
,1600
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000008
,500000008
,'2022-04-06 10:09:00'
,'08000000I'
,'I'
,'2024-04-06 00:00:00'
,'F'
,38.46
,0
,80
,'Y'
,2000
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000009
,500000009
,'2022-03-02 10:09:00'
,'09000000J'
,'I'
,'2024-03-02 00:00:00'
,'W'
,20.19
,0
,30
,'Y'
,2100
);

INSERT INTO payment_terms
(
 payment_terms_id             
,defendant_account_id          
,posted_date                   
,posted_by                     
,terms_type_code               
,effective_date                
,instalment_period             
,instalment_amount             
,instalment_lump_sum           
,jail_days                     
,extension                     
,account_balance               
)
VALUES
(
 500000010
,500000010
,'2019-02-10 00:00:00'
,'09000000I'
,'P'
,'2021-02-10 00:00:00'
,NULL
,NULL
,NULL
,14
,'N'
,0
);
 