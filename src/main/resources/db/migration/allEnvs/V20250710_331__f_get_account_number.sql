CREATE OR REPLACE FUNCTION f_get_account_number(
    IN pi_business_unit_id   draft_accounts.business_unit_id%TYPE,
    IN pi_account_index_type VARCHAR)
    RETURNS draft_accounts.account_number%TYPE
AS $$
/**
* CGI OPAL Program
*
* MODULE      : f_get_account_number.sql
*
* DESCRIPTION : Generate an account number based on a given business unit.
* 
* PARAMETERS  : pi_business_unit_id   - The business_unit_id the generated account_number is related to
*               pi_account_index_type - The type of account (i.e. target table) the generated account is intended for 
*                                       Expected values: defendant_accounts, creditor_accounts or miscellaneous_accounts
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------
* 08/07/2025    TMc         1.0         PO-899 Generate an account number based on a given business unit
*
**/
DECLARE
    c_seq_min_value    CONSTANT VARCHAR := '000001';
    c_seq_max_value    CONSTANT VARCHAR := '999999';
    c_allowed_retries  CONSTANT INTEGER := 5;
    v_retries                   INTEGER := 0;
    v_yy                        VARCHAR;
    v_min_account_number        account_number_index.account_number%TYPE;
    v_max_account_number        account_number_index.account_number%TYPE;
    v_account_number_gap        account_number_index.account_number%TYPE;
    v_account_number_partial    account_number_index.account_number%TYPE;
    v_account_number            account_number_index.account_number%TYPE;
    v_check_letter              VARCHAR;
    v_error_msg                 VARCHAR;
BEGIN

    --RAISE INFO 'pi_business_unit_id = %', pi_business_unit_id;
    --RAISE INFO 'pi_account_index_type = %', pi_account_index_type;
    --RAISE INFO 'c_seq_min_value = %', c_seq_min_value;
    --RAISE INFO 'c_seq_max_value = %', c_seq_max_value;
    
    --Loop until successful insert into account_number_index
    LOOP 
        v_yy := TO_CHAR(NOW(), 'YY');

        --RAISE INFO 'v_yy = %', v_yy;

        --Get minimum and maximum account_number for the passed BU and greater than the current year's minimum value, without the check character
        SELECT MIN(account_number), MAX(account_number)
          FROM account_number_index
         WHERE account_number > v_yy || c_seq_min_value
           AND business_unit_id = pi_business_unit_id
        INTO v_min_account_number, v_max_account_number;
        
        --RAISE INFO 'Min account_number = %', v_min_account_number;
        --RAISE INFO 'Max account_number = %', v_max_account_number;
        
        IF v_max_account_number IS NULL THEN
            --Account number, within current or future years, NOT found so set to min value for the current year
            --RAISE INFO 'Max value NOT found. Starting from min sequence for the current year [%]', v_yy || c_seq_min_value;
    
            v_account_number_partial := v_yy || c_seq_min_value;
        ELSE
            --Get YY from the max account number. It may have already rolled into next years range
            v_yy := LEFT(v_max_account_number, 2);
    
            --Check if max value reached, if it hasn't, then increment sequence value, otherwise check for gaps before rolling into next year
            IF SUBSTRING(v_max_account_number FROM 3 FOR 6) < c_seq_max_value THEN
                --Within range so increment value
                --RAISE INFO 'Within the range. Incrementing sequence';
                v_account_number_partial := v_yy || LPAD((SUBSTRING(v_max_account_number FROM 3 FOR 6)::INT + 1)::VARCHAR, 6, '0');
            ELSE
                --RAISE INFO 'Max value has been reached. Checking for gaps';
                
                --Check if account_number for min value is available. Gap SQL won't return records if gaps exist at the beginning of the range
                IF LEFT(v_min_account_number, 8) = v_yy || c_seq_min_value THEN
                    --Min sequence value exists, check for gaps within the range before rolling into next years range
                    --RAISE INFO 'Min sequence value exists, checking for other gaps within the range';
    
                    SELECT (MIN(account_number) + 1)::VARCHAR AS next_available_account_number  
                      FROM (
                            SELECT LEFT(account_number, 8)::INT AS account_number, lead(LEFT(account_number, 8)::INT) OVER ( PARTITION BY business_unit_id ORDER BY account_number) next_account_number
                              FROM account_number_index
                             WHERE account_number > v_yy || c_seq_min_value
                               AND business_unit_id = pi_business_unit_id
                           ) maxan
                     WHERE account_number + 1 != next_account_number
                    INTO v_account_number_gap;
    
                    IF v_account_number_gap IS NULL THEN
                        --No gaps found so roll into next years range
                        --RAISE INFO 'No gaps found so rolling into next years range';
                        v_account_number_partial := (v_yy::INT + 1) || c_seq_min_value;
                    ELSE
                        --Gap was found
                        --RAISE INFO 'Gap found: %', v_account_number_gap;
                        v_account_number_partial := v_account_number_gap;
                    END IF;
                ELSE
                    --Min sequence value doesn't exist so use it
                    --RAISE INFO 'Gap found. Minimum sequence can be used';
                    v_account_number_partial := v_yy || c_seq_min_value;
                END IF;
            END IF;
        END IF;
    
        --RAISE INFO 'v_account_number_partial = %', v_account_number_partial;
    
        v_check_letter := f_get_check_letter(v_account_number_partial);
        v_account_number := v_account_number_partial || v_check_letter;
        
        BEGIN
            RAISE INFO 'Inserting record into ACCOUNT_NUMBER_INDEX for account_number: %, BU: %', v_account_number, pi_business_unit_id;
        
            INSERT INTO account_number_index(account_number_index_id, business_unit_id, account_number, account_index_type)
            VALUES ( NEXTVAL('account_number_index_seq')
                   , pi_business_unit_id
                   , v_account_number
                   , pi_account_index_type
                   );
 
            --Exit loop on success
            EXIT;
        EXCEPTION 
            WHEN UNIQUE_VIOLATION THEN
                v_error_msg := format('Error in f_get_account_number. Unique violation inserting Account number = %s, BU = %s. Error: %s - %s', v_account_number, pi_business_unit_id, SQLSTATE, SQLERRM);
                RAISE WARNING '%', v_error_msg;

                v_retries := v_retries + 1;

                IF v_retries >= c_allowed_retries THEN
                    RAISE EXCEPTION '%', v_error_msg;
                END IF;
        END;
    END LOOP;

    RETURN v_account_number;
END;
$$ LANGUAGE plpgsql;
