/**
* OPAL Program
*
* MODULE      : p_adjust_control_totals_unit_test.sql
*
* DESCRIPTION : Unit tests for the stored procedure p_adjust_control_totals.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 13/11/2026    S Akinkugbe       1.0         PO-3461 Unit tests for p_adjust_control_totals
*
**/


----------------------------------------------------------------------------------------------------------------------
-- Cleanup data before tests
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup data before tests ===';

   
     DELETE FROM control_totals
     WHERE business_unit_id IN(9971,9972); 
     
     DELETE FROM business_units
     WHERE business_unit_id IN(9971,9972);   

    RAISE NOTICE 'Cleanup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test setup
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN


    RAISE NOTICE '=== Setting up test data ===';
    
    INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
    VALUES (9971, 'PO-3461 Unit Tests', 'U971', 'Accounting Division'),
           (9972, 'PO-3461 Unit Tests 4', 'U972', 'Accounting Division');
    

    RAISE NOTICE 'Test data setup completed';
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 1: Check composite data type
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
DECLARE
    v_type_name VARCHAR(35);
BEGIN
    RAISE NOTICE '=== TEST 1: Check existence of Composite data type ===';

   SELECT
     t.typname AS type_name
     INTO STRICT v_type_name
 FROM pg_type t
JOIN pg_namespace n
    ON n.oid = t.typnamespace
WHERE t.typname ='control_total_item_amount_type'
and t.typtype ='c';
    
   
    ASSERT v_type_name= 'control_total_item_amount_type', 'Composite data type control_total_item_amount_type must exist';

    RAISE NOTICE 'TEST 1 PASSED';
EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 1 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
END $$;

----------------------------------------------------------------------------------------------------------------------
-- Test 2: Adjust control totals for CT
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9971;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= NULL;
    v_account_count control_totals.amount%TYPE:= NULL; 
 BEGIN
    RAISE NOTICE '=== TEST 2: Adjust control totals for CT ===';
    
      v_item_amounts:= ARRAY[('CT',208,100)];
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count);
                                   
      ASSERT v_item_amounts[1].item_number=(SELECT item_number FROM control_totals WHERE business_unit_id=v_business_unit_id),
      'Item number in array must match with control_totals.item_number';
                                   
        RAISE NOTICE 'TEST 2 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 2 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;
----------------------------------------------------------------------------------------------------------------------
-- Test 3: djust control totals for WOT
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9971;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= NULL;
    v_account_count control_totals.amount%TYPE:= NULL; 
 BEGIN
    RAISE NOTICE '=== TEST 3: Adjust control totals for WOT ===';
    
      v_item_amounts:= ARRAY[('WOT',1602,100)];
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count);
                                   
      ASSERT v_item_amounts[1].item_number=(SELECT item_number FROM control_totals WHERE business_unit_id=v_business_unit_id AND item_number=1602),
      'Item number in array must match with control_totals.item_number';
                                   
        RAISE NOTICE 'TEST 3 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 3 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;
----------------------------------------------------------------------------------------------------------------------
-- Test 4: Control totals for CT & WOT
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9972;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= NULL;
    v_account_count control_totals.amount%TYPE:= NULL; 
 BEGIN
    RAISE NOTICE '=== TEST 4: Adjust control totals for both CT and WOT ===';
    
      v_item_amounts:= ARRAY[('CT',1603,200),('CT',1012,100)];
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count);
                                   
      ASSERT v_item_amounts[1].item_number=(SELECT item_number FROM control_totals WHERE business_unit_id=v_business_unit_id AND item_number=1603),
      'CT Item number in array must match with control_totals.item_number';
      ASSERT v_item_amounts[2].item_number=(SELECT item_number FROM control_totals WHERE business_unit_id=v_business_unit_id AND item_number=1012),
      'WOT Item number in array must match with control_totals.item_number';
                                   
        RAISE NOTICE 'TEST 4 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 4 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;

----------------------------------------------------------------------------------------------------------------------
-- Test 5: Update associated record type and record id
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9972;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= NULL;
    v_account_count control_totals.amount%TYPE:= NULL;
    v_associated_record_type control_totals.associated_record_type%TYPE:='suspense_transactions'; 
    v_associated_record_id control_totals.associated_record_id%TYPE:= 44556;
    v_b4_associated_record_type control_totals.associated_record_type%TYPE;
    v_aft_associated_record_type control_totals.associated_record_type%TYPE;
    v_b4_associated_record_id control_totals.associated_record_id%TYPE;
    v_aft_associated_record_id control_totals.associated_record_id%TYPE;
 BEGIN
    RAISE NOTICE '=== TEST 5: Update associated record type and record id ===';
    
      v_item_amounts:= ARRAY[('CT',1603,50)];
      
      SELECT associated_record_type,associated_record_id 
      STRICT INTO v_b4_associated_record_type,v_b4_associated_record_id
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count,
                                   pi_associated_record_type:=v_associated_record_type,
                                   pi_associated_record_id:=v_associated_record_id);
                                   
      SELECT associated_record_type,associated_record_id 
      STRICT INTO v_aft_associated_record_type,v_aft_associated_record_id
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
                                   
      ASSERT (v_b4_associated_record_type IS NULL AND v_b4_associated_record_id IS NULL),
      'Associated_record_type and associated_recored_id should be null before update';
      ASSERT (v_aft_associated_record_type=v_associated_record_type AND v_aft_associated_record_id=v_associated_record_id) ,
      'Associated_record_type and associated_recored_id must match with values in the table';
                                   
        RAISE NOTICE 'TEST 5 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 5 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;

----------------------------------------------------------------------------------------------------------------------
-- Test 6: Attempt to overwrite associated record type and id with null
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9972;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= NULL;
    v_account_count control_totals.amount%TYPE:= NULL;
    v_associated_record_type control_totals.associated_record_type%TYPE:=NULL; 
    v_associated_record_id control_totals.associated_record_id%TYPE:= NULL;
    v_b4_associated_record_type control_totals.associated_record_type%TYPE;
    v_aft_associated_record_type control_totals.associated_record_type%TYPE;
    v_b4_associated_record_id control_totals.associated_record_id%TYPE;
    v_aft_associated_record_id control_totals.associated_record_id%TYPE;
 BEGIN
    RAISE NOTICE '=== TEST 6: Update existing associated record type and record id with null value ===';
    
      v_item_amounts:= ARRAY[('CT',1603,20)];
      
      SELECT associated_record_type,associated_record_id 
      STRICT INTO v_b4_associated_record_type,v_b4_associated_record_id
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count,
                                   pi_associated_record_type:=v_associated_record_type,
                                   pi_associated_record_id:=v_associated_record_id);
                                   
      SELECT associated_record_type,associated_record_id 
      STRICT INTO v_aft_associated_record_type,v_aft_associated_record_id
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
                                   
     
      ASSERT (v_aft_associated_record_type=v_b4_associated_record_type AND v_aft_associated_record_id=v_b4_associated_record_id) ,
      'Associated_record_type and associated_recored_id must not change';
                                   
        RAISE NOTICE 'TEST 6 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 6 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;

----------------------------------------------------------------------------------------------------------------------
-- Test 7: supply value for account_count and counter_item_number
----------------------------------------------------------------------------------------------------------------------
DO
LANGUAGE 'plpgsql' $$
DECLARE
    
    v_business_unit_id business_units.business_unit_id%TYPE:=9972;
    v_item_amounts control_total_item_amount_type[];
    v_counter_item_number control_totals.item_number%TYPE:= 1012;
    v_account_count control_totals.amount%TYPE:= 100;
    v_associated_record_type control_totals.associated_record_type%TYPE:=NULL; 
    v_associated_record_id control_totals.associated_record_id%TYPE:= NULL;
    v_wot_b4_amount  control_totals.amount%TYPE;
    v_wot_aft_amount  control_totals.amount%TYPE;
    
 BEGIN
    RAISE NOTICE '=== TEST 7: Apply counter_item_number and account_count ===';
    
      v_item_amounts:= ARRAY[('WOT',1012,70)];
      
      SELECT amount 
      STRICT INTO v_wot_b4_amount
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
    
      CALL p_adjust_control_totals(pi_business_unit_id:= v_business_unit_id,
                                   pi_item_amounts := v_item_amounts,
                                   pi_counter_item_number:= v_counter_item_number,
                                   pi_account_count:= v_account_count,
                                   pi_associated_record_type:=v_associated_record_type,
                                   pi_associated_record_id:=v_associated_record_id);
                                   
      SELECT amount 
      STRICT INTO v_wot_aft_amount
      FROM control_totals
      WHERE business_unit_id=v_business_unit_id
      AND item_number=v_item_amounts[1].item_number;
                                   
     
      ASSERT ((v_wot_b4_amount + v_item_amounts[1].amount + v_account_count) =v_wot_aft_amount) ,
      'Sum of previous amount, amount in array and account count must equal to 270';
                                   
        RAISE NOTICE 'TEST 7 PASSED';
 EXCEPTION
    WHEN ASSERT_FAILURE THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        RAISE NOTICE 'TEST 7 FAILED: % - %', SQLSTATE, SQLERRM;
        RAISE;                           
                                   
  END
$$;

----------------------------------------------------------------------------------------------------------------------
-- Cleanup test data
----------------------------------------------------------------------------------------------------------------------
DO LANGUAGE 'plpgsql' $$
BEGIN
    RAISE NOTICE '=== Cleanup test data ===';

    
    DELETE FROM control_totals
     WHERE business_unit_id IN(9971,9972);  
     
    DELETE FROM business_units
     WHERE business_unit_id IN(9971,9972);  

    RAISE NOTICE 'Test data cleanup completed';
END $$;

