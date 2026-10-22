CREATE OR REPLACE PROCEDURE public.p_insert_allocations(
    IN pi_imposition_id                allocations.imposition_id%TYPE,
    IN pi_imposition_allocation_amount allocations.allocated_amount%TYPE,
    IN pi_transaction_type             allocations.transaction_type%TYPE,
    IN pi_allocation_function          allocations.allocation_function%TYPE,
    IN pi_defendant_transaction_id     allocations.defendant_transaction_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_insert_allocations.sql
*
* DESCRIPTION : Insert an allocation for an imposition and defendant transaction.
*
* PARAMETERS  : pi_imposition_id                - Imposition receiving the allocated payment
*               pi_imposition_allocation_amount - Positive amount allocated to the imposition
*               pi_transaction_type             - Transaction type recorded for the allocation
*               pi_allocation_function          - Function responsible for creating the allocation
*               pi_defendant_transaction_id     - Defendant transaction linked to the allocation
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 16/10/2026    C Cho          1.0         PO-3449 - Create p_insert_allocations
*
**/
DECLARE
    v_pg_exception_detail TEXT;
BEGIN
    IF pi_imposition_id IS NULL THEN
        RAISE EXCEPTION 'Imposition ID cannot be null'
            USING ERRCODE = 'P3701'
                , DETAIL = 'p_insert_allocations: pi_imposition_id is required';
    END IF;

    IF pi_imposition_allocation_amount IS NULL
       OR pi_imposition_allocation_amount <= 0 THEN
        RAISE EXCEPTION 'Imposition allocation amount must be greater than zero'
            USING ERRCODE = 'P3702'
                , DETAIL = 'p_insert_allocations: pi_imposition_allocation_amount must be a positive value';
    END IF;

    IF pi_transaction_type IS NULL THEN
        RAISE EXCEPTION 'Transaction type cannot be null'
            USING ERRCODE = 'P3703'
                , DETAIL = 'p_insert_allocations: pi_transaction_type is required';
    END IF;

    IF NULLIF(BTRIM(pi_allocation_function), '') IS NULL THEN
        RAISE EXCEPTION 'Allocation function cannot be null or blank'
            USING ERRCODE = 'P3704'
                , DETAIL = 'p_insert_allocations: pi_allocation_function is required';
    END IF;

    IF pi_defendant_transaction_id IS NULL THEN
        RAISE EXCEPTION 'Defendant transaction ID cannot be null'
            USING ERRCODE = 'P3705'
                , DETAIL = 'p_insert_allocations: pi_defendant_transaction_id is required';
    END IF;

    PERFORM 1
      FROM public.impositions
     WHERE imposition_id = pi_imposition_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Imposition % was not found', pi_imposition_id
            USING ERRCODE = 'P3706'
                , DETAIL = 'p_insert_allocations: no imposition exists for the passed pi_imposition_id';
    END IF;

    PERFORM 1
      FROM public.defendant_transactions
     WHERE defendant_transaction_id = pi_defendant_transaction_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Defendant transaction % was not found', pi_defendant_transaction_id
            USING ERRCODE = 'P3707'
                , DETAIL = 'p_insert_allocations: no defendant transaction exists for the passed pi_defendant_transaction_id';
    END IF;

    INSERT INTO public.allocations (
          allocation_id
        , imposition_id
        , allocated_date
        , allocated_amount
        , transaction_type
        , allocation_function
        , defendant_transaction_id
    )
    VALUES (
          NEXTVAL('public.allocation_id_seq')
        , pi_imposition_id
        , CLOCK_TIMESTAMP()
        , pi_imposition_allocation_amount
        , pi_transaction_type
        , pi_allocation_function
        , pi_defendant_transaction_id
    );

EXCEPTION
    WHEN SQLSTATE 'P3701' OR SQLSTATE 'P3702' OR SQLSTATE 'P3703'
      OR SQLSTATE 'P3704' OR SQLSTATE 'P3705' OR SQLSTATE 'P3706'
      OR SQLSTATE 'P3707' THEN
        RAISE NOTICE 'Error in p_insert_allocations: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_insert_allocations: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE;
END;
$procedure$;
