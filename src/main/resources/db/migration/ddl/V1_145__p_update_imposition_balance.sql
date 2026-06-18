/**
*
* OPAL Program
*
* MODULE      : p_update_imposition_balance.sql
*
* DESCRIPTION : Create p_update_imposition_balance procedure for Admin Write Off
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------
* 11/06/2026    P Brumby    1.0         PO-3457 Create p_update_imposition_balance procedure for Admin Write Off
*
**/

CREATE OR REPLACE PROCEDURE p_update_imposition_balance(
    IN pi_imposition_id impositions.imposition_id%TYPE,
    IN pi_write_off_amount defendant_accounts.amount_imposed%TYPE
)
LANGUAGE 'plpgsql'
AS $BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_update_imposition_balance.sql
*
* DESCRIPTION : Procedure to update imposition balances during Admin Write Off processing.
*
* PARAMETERS : pi_imposition_id          - The imposition_id of the IMPOSITIONS record to be updated
*            : pi_write_off_amount       - The positive write off amount to be applied to the IMPOSITIONS.paid_amount
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 11/06/2026    P Brumby    1.0         PO-3457 Initial version - Update imposition balance for write off.
*
**/
DECLARE
    v_imposed_amount        impositions.imposed_amount%TYPE;
    v_paid_amount           impositions.paid_amount%TYPE;
    v_completed             impositions.completed%TYPE;
    v_outstanding_balance   defendant_accounts.amount_imposed%TYPE;
    v_resultant_balance     defendant_accounts.amount_imposed%TYPE;
    v_pg_exception_detail   TEXT;
BEGIN
    RAISE INFO 'p_update_imposition_balance: pi_imposition_id = %, pi_write_off_amount = %', pi_imposition_id, pi_write_off_amount;

    IF pi_imposition_id IS NULL THEN
        RAISE EXCEPTION 'Imposition ID cannot be null'
            USING ERRCODE = 'P3101'
                , DETAIL = 'p_update_imposition_balance: pi_imposition_id is required';
    END IF;

    IF pi_write_off_amount IS NULL THEN
        RAISE EXCEPTION 'Write off amount cannot be null'
            USING ERRCODE = 'P3102'
                , DETAIL = 'p_update_imposition_balance: pi_write_off_amount is required';
    END IF;

    IF pi_write_off_amount <= 0 THEN
        RAISE EXCEPTION 'Write off amount must be greater than zero'
            USING ERRCODE = 'P3103'
                , DETAIL = 'p_update_imposition_balance: pi_write_off_amount must be a positive value';
    END IF;

    SELECT imposed_amount
         , paid_amount
         , completed
      INTO v_imposed_amount
         , v_paid_amount
         , v_completed
      FROM impositions
     WHERE imposition_id = pi_imposition_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Imposition % not found', pi_imposition_id
            USING ERRCODE = 'P3104'
                , DETAIL = 'p_update_imposition_balance: no imposition record exists for the passed pi_imposition_id';
    END IF;

    RAISE INFO 'v_imposed_amount = %', v_imposed_amount;
    RAISE INFO 'v_paid_amount = %', v_paid_amount;
    RAISE INFO 'v_completed = %', v_completed;

    /*
     * Current design decision for ETL-loaded impositions:
     * - completed = NULL is allowed
     * - NULL is not treated as an invalid state
     * - completed is only set to TRUE where the resultant balance is zero
     * This may need to be revised if the ETL/completed-state rule changes.
     */
    IF v_completed = TRUE THEN
        RAISE EXCEPTION 'Imposition % is already completed', pi_imposition_id
            USING ERRCODE = 'P3105'
                , DETAIL = 'p_update_imposition_balance: completed impositions cannot be written off again';
    END IF;

    v_outstanding_balance := v_imposed_amount + v_paid_amount;
    RAISE INFO 'v_outstanding_balance = %', v_outstanding_balance;

    IF pi_write_off_amount > ABS(v_outstanding_balance) THEN
        RAISE EXCEPTION 'Write off amount % exceeds outstanding balance for imposition %', pi_write_off_amount, pi_imposition_id
            USING ERRCODE = 'P3106'
                , DETAIL = 'p_update_imposition_balance: outstanding balance is calculated as imposed_amount + paid_amount';
    END IF;

    v_resultant_balance := v_outstanding_balance + pi_write_off_amount;
    RAISE INFO 'v_resultant_balance = %', v_resultant_balance;

    UPDATE impositions
       SET paid_amount = paid_amount + pi_write_off_amount,
           completed   = CASE
                           /*
                            * Current design decision for ETL-loaded impositions with completed = NULL:
                            * only force completed to TRUE where resultant balance = 0;
                            * otherwise preserve the existing value, including NULL.
                            */
                           WHEN v_resultant_balance = 0 THEN TRUE
                           ELSE completed
                         END
     WHERE imposition_id = pi_imposition_id;

EXCEPTION
    WHEN SQLSTATE 'P3101' OR SQLSTATE 'P3102' OR SQLSTATE 'P3103' OR SQLSTATE 'P3104' OR SQLSTATE 'P3105' OR SQLSTATE 'P3106' THEN
        RAISE NOTICE 'Error in p_update_imposition_balance: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_update_imposition_balance: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_update_imposition_balance: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_update_imposition_balance
    IS 'Procedure to update imposition paid_amount and completed flag during Admin Write Off processing.';
