/**
* OPAL Program
*
* MODULE      : insert_defendant_accounts.sql
*
* DESCRIPTION : Inserts rows of data into the DEFENDANT_ACCOUNTS table. 
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 05/01/2024    A Dennis    1.0         PO-147 Inserts one row of data into the DEFENDANT_ACCOUNTS table
*
**/
INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000000
,5000
,'10000000000A'
,'2023-11-03 16:05:10'
,500000000
,700.58
,200.00
,500.58
,'L'
,NULL
,500000000
,500000000
,'2024-01-04 18:06:11'
,'2024-01-02 17:08:09'
,'2024-01-03 12:00:12'
,'REM'
,'Kingston-upon-Thames Mags Court'
,NULL
,NULL
,'N'
,'N'
,14
,21
,2723
,500000000
,2022
,'GB pound sterling'
,700.00
,'Y'
,'2023-12-18 00:00:00'
,'2023-12-19 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2024-01-01 00:00:00'
,'11111111A'
,'090000000A'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000001
,5001
,'20000000000B'
,'2022-10-04 15:05:10'
,500000001
,800.58
,400.00
,400.58
,'L'
,NULL
,500000001
,500000001
,'2023-02-04 11:06:11'
,'2023-01-02 13:08:09'
,'2023-03-03 14:00:12'
,'ENF'
,'Reedley Family Court'
,NULL
,NULL
,'N'
,'N'
,14
,21
,2607
,500000001
,2216
,'GB pound sterling'
,800.00
,'Y'
,'2023-11-17 00:00:00'
,'2023-11-18 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-05-05 00:00:00'
,'222222222B'
,'080000000B'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000002
,5002
,'30000000000C'
,'2022-09-03 15:05:10'
,500000002
,1000.58
,800.00
,200.58
,'L'
,NULL
,500000002
,500000002
,'2023-02-04 11:06:11'
,'2023-01-02 13:08:09'
,'2023-03-03 14:00:12'
,'ENF'
,'Lancaster County Court'
,NULL
,NULL
,'N'
,'N'
,14
,21
,2631
,500000002
,2232
,'GB pound sterling'
,1100.00
,'Y'
,'2023-10-15 00:00:00'
,'2023-10-16 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-04-04 00:00:00'
,'322222222C'
,'070000000C'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000003
,5003
,'30000000000C'
,'2022-09-03 15:05:10'
,500000003
,1100.58
,900.00
,200.58
,'L'
,NULL
,500000003
,500000003
,'2023-08-28 11:06:11'
,'2023-07-27 13:08:09'
,'2023-09-26 14:00:12'
,'ACT'
,'Aldridge Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,14
,21
,2679
,500000003
,2651
,'GB pound sterling'
,1100.00
,'Y'
,'2023-09-17 00:00:00'
,'2023-09-18 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-06-06 00:00:00'
,'422222222D'
,'080000000D'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000004
,5004
,'40000000000D'
,'2022-10-04 15:05:10'
,500000004
,1300.00
,1000.00
,300.00
,'L'
,NULL
,500000004
,500000004
,'2023-09-27 11:06:11'
,'2023-08-26 13:08:09'
,'2023-10-25 14:00:12'
,'REM'
,'City of Salford Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,14
,21
,2223
,500000004
,2078
,'GB pound sterling'
,1200.00
,'Y'
,'2023-10-21 00:00:00'
,'2023-10-22 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-07-07 00:00:00'
,'522222222E'
,'090000000E'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000005
,5005
,'50000000000E'
,'2022-11-05 11:05:10'
,500000005
,1400.00
,100.00
,400.00
,'L'
,NULL
,500000005
,500000005
,'2023-11-27 12:06:11'
,'2023-09-26 11:08:09'
,'2023-11-25 15:00:12'
,'REM'
,'Chester Youth Court'
,NULL
,NULL
,'N'
,'N'
,15
,20
,2396
,500000005
,1755
,'GB pound sterling'
,1300.00
,'Y'
,'2023-11-21 00:00:00'
,'2023-12-22 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-06-06 00:00:00'
,'622222222F'
,'010000000F'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000006
,5006
,'50000000000F'
,'2022-12-06 09:06:00'
,500000006
,1500.00
,100.00
,400.00
,'L'
,NULL
,500000006
,500000006
,'2023-05-28 12:06:11'
,'2023-04-25 11:08:09'
,'2023-10-24 15:00:12'
,'ENF'
,'North Sefton Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,15
,20
,2406
,500000006
,2320
,'GB pound sterling'
,1700.00
,'Y'
,'2023-03-01 00:00:00'
,'2023-04-02 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-02-05 00:00:00'
,'722222222G'
,'020000000G'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000007
,5007
,'60000000000G'
,'2022-08-07 09:06:00'
,500000007
,1600.00
,800.00
,800.00
,'L'
,NULL
,500000007
,500000007
,'2023-05-19 12:06:11'
,'2023-03-20 11:08:09'
,'2023-11-20 15:00:12'
,'ACT'
,'Grimsby Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,16
,22
,2646
,500000007
,2160
,'GB pound sterling'
,1900.00
,'Y'
,'2023-04-02 00:00:00'
,'2023-05-03 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-05-06 00:00:00'
,'822222222H'
,'030000000H'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000008
,5008
,'70000000000H'
,'2022-04-06 10:09:00'
,500000008
,2000.00
,900.00
,1100.00
,'L'
,NULL
,500000008
,500000008
,'2023-05-20 12:06:11'
,'2023-03-21 11:08:09'
,'2023-11-21 15:00:12'
,'FIN'
,'Medway Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,16
,22
,2409
,500000008
,3119
,'GB pound sterling'
,2200.00
,'Y'
,'2023-09-01 00:00:00'
,'2023-08-06 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-06-07 00:00:00'
,'922222222I'
,'040000000I'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000009
,5009
,'80000000000I'
,'2022-03-02 10:09:00'
,500000009
,2100.00
,1100.00
,1000.00
,'L'
,NULL
,500000009
,500000009
,'2023-11-01 16:06:11'
,'2023-04-11 11:08:09'
,'2023-10-20 13:00:12'
,'REM'
,'Cardiff Magistrates'' Court'
,NULL
,NULL
,'N'
,'N'
,16
,22
,1838
,500000009
,2831
,'GB pound sterling'
,2300.00
,'Y'
,'2023-04-02 00:00:00'
,'2023-07-05 00:00:00'
,NULL
,NULL
,NULL
,'Y'
,'Y'
,'2023-04-08 00:00:00'
,'102222222J'
,'050000000J'
,NULL
);

INSERT INTO defendant_accounts
(
 defendant_account_id	              
,business_unit_id	                  
,account_number	                    
,imposed_hearing_date	            
,imposing_court_id	                
,amount_imposed	                    
,amount_paid	                    
,account_balance	                
,account_status	                    
,completed_date	                    
,enforcing_court_id	                
,last_hearing_court_id	            
,last_hearing_date	                
,last_movement_date	                
,last_changed_date	                
,last_enforcement	                
,originator_name	                
,originator_reference	            
,originator_type	                
,allow_writeoffs	                
,allow_cheques	                    
,cheque_clearance_period	        
,credit_trans_clearance_period	    
,enf_override_result_id	            
,enf_override_enforcer_id	        
,enf_override_tfo_lja_id	        
,unit_fine_detail	                
,unit_fine_value	                
,collection_order	                
,collection_order_date	            
,further_steps_notice_date	        
,confiscation_order_date	        
,fine_registration_date	            
,suspended_committal_date           
,consolidated_account_type	        
,payment_card_requested	            
,payment_card_requested_date	    
,payment_card_requested_by	        
,prosecutor_case_reference	        
,enforcement_case_status	            
)
VALUES
(
 500000010
,5009
,'90000000000J'
,'2019-02-10 00:00:00'
,500000009
,1800.00
,1800.00
,0
,'C'
,'2021-02-15 00:00:00'
,500000009
,500000009
,'2020-12-02 00:00:00'
,'2019-02-10 00:00:00'
,'2021-02-20 00:00:00'
,'REM'
,'Cardiff Magistrates'' Court'
,NULL
,'FP'
,'Y'
,'Y'
,16
,22
,1838
,500000009
,2831
,'GB pound sterling'
,2000.00
,'N'
,NULL
,NULL
,NULL
,NULL
,NULL
,'Y'
,'Y'
,NULL
,'102222222J'
,'050000000L'
,NULL
);
