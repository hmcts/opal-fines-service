CREATE OR REPLACE PROCEDURE p_update_impositions(
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
* DESCRIPTION : Apply an allocated payment amount to an imposition.
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
    v_pg_exception_detail TEXT;
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

    PERFORM 1
      FROM impositions
     WHERE imposition_id = pi_imposition_id
       FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Imposition % was not found', pi_imposition_id
            USING ERRCODE = 'P3603'
                , DETAIL = 'p_update_impositions: no imposition exists for the passed pi_imposition_id';
    END IF;

    UPDATE impositions
       SET paid_amount = paid_amount + pi_imposition_allocation_amount
         , completed = CASE
                           WHEN paid_amount + pi_imposition_allocation_amount = -imposed_amount THEN TRUE
                           ELSE completed
                       END
     WHERE imposition_id = pi_imposition_id;

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
