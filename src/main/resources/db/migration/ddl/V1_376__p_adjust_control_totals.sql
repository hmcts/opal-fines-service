
CREATE OR REPLACE PROCEDURE p_adjust_control_totals(
IN pi_business_unit_id defendant_accounts.business_unit_id%TYPE,
IN pi_item_amounts control_total_item_amount_type[],
IN pi_counter_item_number control_totals.item_number%TYPE,
IN pi_account_count control_totals.amount%TYPE,
IN pi_associated_record_type control_totals.associated_record_type%TYPE DEFAULT NULL,
IN pi_associated_record_id control_totals.associated_record_id%TYPE DEFAULT NULL)
language plpgsql
AS $procedure$
/** 

 * OPAL Program 

 * 

 * MODULE      : p_adjust_control_totals

 * 

 * DESCRIPTION : Common procedure to handle all Creates or Updates  

 *               on CONTROL_TOTALS as a reusable Adjust Control Totals procedure

 *               across multiple Opal processes or functions

 * 

 * PARAMETERS  : pi_business_unit_id       - Business unit Id related to the control totals being adjusted

 *               pi_item_amounts           - item collection per imposition

 *               pi_counter_item_number    - Item number for Counter Control Total

 *               pi_account_count          - Amount for Counter Control Total

 *               pi_associated_record_type  - Type of associated record e.g. 'defendant_accounts' or 'suspense_transactions' etc

 *               pi_associated_record_id.  - Id of associated record  

 * VERSION HISTORY: 

 * 

 * Date        Author      Version  Nature of Change 

 * 13/11/2026  S Akinkugbe   1.0     1.0  PO-3461 Initial implementation - PO-10795 Amendment to p_adjust_control_totals to add associated_record_type and associated_record_id
 *                                 

 **/ 

 
DECLARE
v_pg_exception_detail TEXT;
c_control_type_ct CONSTANT control_total_item_amount_type.control_total_type%type:='CT';
c_control_type_wot CONSTANT control_total_item_amount_type.control_total_type%type:='WOT';
BEGIN 
        -------------------------------------------------------------------- 

        -- Step 1: Check if the item amounts array is not empty. 

        -------------------------------------------------------------------- 
      IF ARRAY_LENGTH(pi_item_amounts,1) <= 0 THEN
	    RAISE EXCEPTION 'Array of records item amounts cannot be empty'
		USING ERRCODE='P5001',
		DETAIL ='p_adjust_control_totals: pi_item_amounts is required';
       END IF;
      
        -------------------------------------------------------------------- 

        -- Step 2: Loop through the item amounts array and perform the necessary insert or update operations on the control_totals table. 

        -------------------------------------------------------------------- 
	  FOR i IN 1..CARDINALITY(pi_item_amounts) LOOP
	
	      RAISE NOTICE 'Control Total Type: %', pi_item_amounts[i].control_total_type;
		  RAISE NOTICE 'Item Number: %', pi_item_amounts[i].item_number;
		  RAISE NOTICE 'Amount: %', pi_item_amounts[i].amount;

           -------------------------------------------------------------------- 

            -- Step 3: Check if the control total type is 'CT' or 'WOT' 
            --        and perform the corresponding insert or update operation on the control_totals table.

           --------------------------------------------------------------------   
		  IF (pi_item_amounts[i].control_total_type=c_control_type_ct) THEN		  
		      PERFORM 1 FROM control_totals
		       WHERE business_unit_id =pi_business_unit_id
		       AND item_number = pi_item_amounts[i].item_number;
		     
		    IF NOT FOUND THEN		     
		        INSERT into control_totals (control_total_id, 
                                    business_unit_id, 
                                    item_number, 
                                    amount, 
                                    associated_record_type,
                                    associated_record_id, 
                                    ct_report_instance_id,
                                    qe_report_instance_id 
                                    )
		         VALUES (nextval('control_total_id_seq'), 
		                           pi_business_unit_id,
		                           pi_item_amounts[i].item_number, 
		                           pi_item_amounts[i].amount,
		                           CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE NULL END,
		                           CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE NULL END, 
		                           NULL, 
		                           NULL);	     
		     ELSE
                 UPDATE control_totals
		         SET  amount = amount + pi_item_amounts[i].amount,
		              associated_record_type= CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE associated_record_type END,
                      associated_record_id = CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE associated_record_id END
		        WHERE business_unit_id =pi_business_unit_id
		        AND item_number =pi_item_amounts[i].item_number;
	
	        END IF;
	     ELSIF (pi_item_amounts[i].control_total_type=c_control_type_wot) THEN	      
	        PERFORM 1 FROM control_totals
		    WHERE business_unit_id =pi_business_unit_id
		    AND item_number = pi_item_amounts[i].item_number;
			
		    IF NOT FOUND THEN
		      INSERT into control_totals (control_total_id, 
                                        business_unit_id, 
                                        item_number, 
                                        amount, 
                                        associated_record_type,
                                        associated_record_id, 
                                        ct_report_instance_id, 
                                        qe_report_instance_id)
		            VALUES (nextval('control_total_id_seq'), 
		                           pi_business_unit_id,
		                           pi_item_amounts[i].item_number, 
		                           pi_item_amounts[i].amount,
		                           CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE NULL END,
		                           CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE NULL END, 
		                           NULL, 
		                           NULL);
		    ELSE
		            
			  UPDATE control_totals 
			  SET amount = amount + pi_item_amounts[i].amount,
				  associated_record_type = CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE associated_record_type END,
				  associated_record_id = CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE associated_record_id END
			  WHERE business_unit_id = pi_business_unit_id
			  AND item_number = pi_item_amounts[i].item_number;

		         
		   END IF;
		END IF;      
     END LOOP; 
      --------------------------------------------------------------------------- 

            -- Step 4: Check if the counter item number and account count are not null 
            --         and perform the corresponding insert or update operation on the 
            --         control_totals table.

      -----------------------------------------------------------------------------        
        IF (pi_counter_item_number IS NOT NULL AND pi_account_count IS NOT NULL) THEN
            PERFORM 1 FROM control_totals
            WHERE business_unit_id=pi_business_unit_id
            AND item_number=pi_counter_item_number;
                
            IF NOT FOUND THEN
            INSERT into control_totals (control_total_id, 
                            business_unit_id, 
                            item_number, 
                            amount, 
                            associated_record_type,
                            associated_record_id, 
                            ct_report_instance_id, 
                            qe_report_instance_id)
                VALUES (nextval('control_total_id_seq'), 
                        pi_business_unit_id,
                        pi_counter_item_number, 
                        pi_account_count,
                        CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE NULL END,
                        CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE NULL END, 
                        NULL, 
                        NULL);
                        
            ELSE
            UPDATE control_totals 
            SET  amount = amount + pi_account_count,
                    associated_record_type = CASE WHEN pi_associated_record_type IS NOT NULL THEN  pi_associated_record_type::t_associated_record_type_enum ELSE associated_record_type END,
                    associated_record_id = CASE WHEN pi_associated_record_id IS NOT NULL THEN  pi_associated_record_id ELSE associated_record_id END
            WHERE business_unit_id = pi_business_unit_id
            AND item_number = pi_counter_item_number;
            END IF;		         
        END IF;	             
EXCEPTION

        WHEN OTHERS THEN        
          GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
          RAISE NOTICE 'Error in p_adjust_control_totals: % - %',SQLSTATE,SQLERRM;
          RAISE NOTICE 'Error details: %',v_pg_exception_detail;
          RAISE;
        

END;
$procedure$;

COMMENT ON PROCEDURE p_adjust_control_totals IS 'Adjust Control Totals Common procedure';
