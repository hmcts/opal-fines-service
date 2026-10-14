CREATE OR REPLACE PROCEDURE public.p_update_defendant_accounts(
    IN pi_defendant_account_id    defendant_accounts.defendant_account_id%TYPE,
    IN pi_payment_amount          defendant_accounts.amount_paid%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_update_defendant_accounts.sql
*
* DESCRIPTION : Apply an allocated payment to a defendant account.
*
* PARAMETERS  : pi_defendant_account_id - Defendant account receiving the allocated payment
*               pi_payment_amount       - Positive payment amount allocated to the account
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 13/10/2026    C Cho          1.0         PO-3447 - Create p_update_defendant_accounts
*
**/
DECLARE
    v_account_balance       defendant_accounts.account_balance%TYPE;
    v_amount_imposed        defendant_accounts.amount_imposed%TYPE;
    v_resultant_balance     defendant_accounts.account_balance%TYPE;
    v_pg_exception_detail   TEXT;
BEGIN
    IF pi_defendant_account_id IS NULL THEN
        RAISE EXCEPTION 'Defendant account ID cannot be null'
            USING ERRCODE = 'P3501',
                  DETAIL = 'p_update_defendant_accounts: pi_defendant_account_id is required';
    END IF;

    IF pi_payment_amount IS NULL THEN
        RAISE EXCEPTION 'Payment amount cannot be null'
            USING ERRCODE = 'P3502',
                  DETAIL = 'p_update_defendant_accounts: pi_payment_amount is required';
    END IF;

    IF pi_payment_amount <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be greater than zero'
            USING ERRCODE = 'P3503',
                  DETAIL = 'p_update_defendant_accounts: pi_payment_amount must be a positive value';
    END IF;

    SELECT account_balance, amount_imposed
    INTO v_account_balance, v_amount_imposed
    FROM public.defendant_accounts
    WHERE defendant_account_id = pi_defendant_account_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Defendant account % was not found', pi_defendant_account_id
            USING ERRCODE = 'P3504',
                  DETAIL = 'p_update_defendant_accounts: no defendant account exists for the passed pi_defendant_account_id';
    END IF;

    IF v_account_balance > 0 THEN
        RAISE EXCEPTION 'Defendant account % has an invalid positive account balance', pi_defendant_account_id
            USING ERRCODE = 'P3505',
                  DETAIL = 'p_update_defendant_accounts: account_balance must be zero or negative before allocation';
    END IF;

    IF pi_payment_amount > ABS(v_account_balance) THEN
        RAISE EXCEPTION 'Payment amount % exceeds the outstanding balance for defendant account %',
                        pi_payment_amount,
                        pi_defendant_account_id
            USING ERRCODE = 'P3506',
                  DETAIL = 'p_update_defendant_accounts: overpayment must be routed to suspense by p_allocate_till';
    END IF;

    v_resultant_balance := v_amount_imposed + pi_payment_amount;

    UPDATE public.defendant_accounts AS da
       SET amount_paid        = da.amount_paid + pi_payment_amount,
           account_balance    = da.amount_imposed + pi_payment_amount,
           last_movement_date = CURRENT_TIMESTAMP,
           completed_date     = CASE
                                    WHEN v_resultant_balance = 0 THEN CURRENT_TIMESTAMP
                                    ELSE da.completed_date
                                END,
           version_number     = da.version_number + 1
     WHERE defendant_account_id = pi_defendant_account_id;

EXCEPTION
    WHEN SQLSTATE 'P3501' OR SQLSTATE 'P3502' OR SQLSTATE 'P3503'
      OR SQLSTATE 'P3504' OR SQLSTATE 'P3505' OR SQLSTATE 'P3506' THEN
        RAISE NOTICE 'Error in p_update_defendant_accounts: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_update_defendant_accounts: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_update_defendant_accounts: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$procedure$;
