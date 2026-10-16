CREATE OR REPLACE PROCEDURE public.p_update_impositions(
    IN pi_imposition_id                impositions.imposition_id%TYPE,
    IN pi_imposition_allocation_amount impositions.paid_amount%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_update_impositions.sql
*
* DESCRIPTION : Apply an allocated payment amount to an imposition and its defendant account.
*
* PARAMETERS  : pi_imposition_id                - Imposition receiving the allocated payment
*               pi_imposition_allocation_amount - Positive payment amount allocated to the imposition
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 15/10/2026    C Cho          1.0         PO-3448 - Create p_update_impositions
*
**/
DECLARE
    v_defendant_account_id defendant_accounts.defendant_account_id%TYPE;
    v_pg_exception_detail  TEXT;
BEGIN
    IF pi_imposition_id IS NULL THEN
        RAISE EXCEPTION 'Imposition ID cannot be null'
            USING ERRCODE = 'P3601'
                , DETAIL = 'p_update_impositions: pi_imposition_id is required';
    END IF;

    IF pi_imposition_allocation_amount IS NULL
       OR pi_imposition_allocation_amount <= 0 THEN
        RAISE EXCEPTION 'Imposition allocation amount must be greater than zero'
            USING ERRCODE = 'P3602'
                , DETAIL = 'p_update_impositions: pi_imposition_allocation_amount must be a positive value';
    END IF;

    SELECT defendant_account_id
      INTO v_defendant_account_id
      FROM public.impositions
     WHERE imposition_id = pi_imposition_id
       FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Imposition % was not found', pi_imposition_id
            USING ERRCODE = 'P3603'
                , DETAIL = 'p_update_impositions: no imposition exists for the passed pi_imposition_id';
    END IF;

    UPDATE public.impositions
       SET paid_amount = paid_amount + pi_imposition_allocation_amount
     WHERE imposition_id = pi_imposition_id;

    UPDATE public.defendant_accounts AS da
       SET amount_paid     = da.amount_paid + pi_imposition_allocation_amount
         , account_balance = da.amount_imposed - (da.amount_paid + pi_imposition_allocation_amount)
     WHERE da.defendant_account_id = v_defendant_account_id;

EXCEPTION
    WHEN SQLSTATE 'P3601' OR SQLSTATE 'P3602' OR SQLSTATE 'P3603' THEN
        RAISE NOTICE 'Error in p_update_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_update_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE;
END;
$procedure$;
