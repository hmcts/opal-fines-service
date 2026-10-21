CREATE OR REPLACE PROCEDURE p_insert_creditor_transactions(
    IN pi_creditor_account_id           creditor_transactions.creditor_account_id%TYPE,
    IN pi_posted_by                     creditor_transactions.posted_by%TYPE,
    IN pi_posted_by_name                creditor_transactions.posted_by_name%TYPE,
    IN pi_transaction_type              creditor_transactions.transaction_type%TYPE,
    IN pi_imposition_allocation_amount  creditor_transactions.transaction_amount%TYPE,
    IN pi_result_id                     creditor_transactions.imposition_result_id%TYPE,
    IN pi_payment_reference             creditor_transactions.payment_reference%TYPE,
    IN pi_imposition_id                 impositions.imposition_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_insert_creditor_transactions.sql
*
* DESCRIPTION : Insert a creditor transaction for an allocated imposition payment.
*
* PARAMETERS  : pi_creditor_account_id          - Creditor account receiving the allocated payment
*               pi_posted_by                    - User identifier posting the transaction
*               pi_posted_by_name               - Name of the user posting the transaction
*               pi_transaction_type             - Creditor transaction type
*               pi_imposition_allocation_amount - Positive amount allocated to the imposition
*               pi_result_id                    - Result identifier associated with the imposition
*               pi_payment_reference            - Payment reference supplied for the payment
*               pi_imposition_id                - Imposition associated with the creditor transaction
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 19/10/2026    C Cho          1.0         PO-3450 - Create p_insert_creditor_transactions
*
**/
DECLARE
    v_pg_exception_detail TEXT;
BEGIN
    IF pi_creditor_account_id IS NULL THEN
        RAISE EXCEPTION 'Creditor account ID cannot be null'
            USING ERRCODE = 'P3801'
                , DETAIL = 'p_insert_creditor_transactions: pi_creditor_account_id is required';
    END IF;

    IF pi_imposition_allocation_amount IS NULL
       OR pi_imposition_allocation_amount <= 0 THEN
        RAISE EXCEPTION 'Imposition allocation amount must be greater than zero'
            USING ERRCODE = 'P3802'
                , DETAIL = 'p_insert_creditor_transactions: pi_imposition_allocation_amount must be a positive value';
    END IF;

    IF NULLIF(BTRIM(pi_result_id), '') IS NULL THEN
        RAISE EXCEPTION 'Result ID cannot be null or blank'
            USING ERRCODE = 'P3803'
                , DETAIL = 'p_insert_creditor_transactions: pi_result_id is required';
    END IF;

    PERFORM 1
      FROM results
     WHERE result_id = pi_result_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Result ID % was not found', pi_result_id
            USING ERRCODE = 'P3808'
                , DETAIL = 'p_insert_creditor_transactions: no result exists for the passed pi_result_id';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by), '') IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null or blank'
            USING ERRCODE = 'P3809'
                , DETAIL = 'p_insert_creditor_transactions: pi_posted_by is required';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by_name), '') IS NULL THEN
        RAISE EXCEPTION 'Posted by name cannot be null or blank'
            USING ERRCODE = 'P3810'
                , DETAIL = 'p_insert_creditor_transactions: pi_posted_by_name is required';
    END IF;

    IF NULLIF(BTRIM(pi_payment_reference), '') IS NULL THEN
        RAISE EXCEPTION 'Payment reference cannot be null or blank'
            USING ERRCODE = 'P3811'
                , DETAIL = 'p_insert_creditor_transactions: pi_payment_reference is required';
    END IF;

    IF pi_transaction_type IS NULL THEN
        RAISE EXCEPTION 'Transaction type cannot be null'
            USING ERRCODE = 'P3804'
                , DETAIL = 'p_insert_creditor_transactions: pi_transaction_type is required';
    END IF;

    IF pi_imposition_id IS NULL THEN
        RAISE EXCEPTION 'Imposition ID cannot be null'
            USING ERRCODE = 'P3805'
                , DETAIL = 'p_insert_creditor_transactions: pi_imposition_id is required';
    END IF;

    PERFORM 1
      FROM creditor_accounts
     WHERE creditor_account_id = pi_creditor_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Creditor account % was not found', pi_creditor_account_id
            USING ERRCODE = 'P3806'
                , DETAIL = 'p_insert_creditor_transactions: no creditor account exists for the passed pi_creditor_account_id';
    END IF;

    PERFORM 1
      FROM impositions
     WHERE imposition_id = pi_imposition_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Imposition % was not found', pi_imposition_id
            USING ERRCODE = 'P3807'
                , DETAIL = 'p_insert_creditor_transactions: no imposition exists for the passed pi_imposition_id';
    END IF;

    INSERT INTO creditor_transactions (
          creditor_transaction_id
        , creditor_account_id
        , posted_date
        , posted_by
        , posted_by_name
        , transaction_type
        , transaction_amount
        , imposition_result_id
        , payment_processed
        , payment_reference
        , status
        , status_date
        , associated_record_type
        , associated_record_id
    )
    VALUES (
          NEXTVAL('creditor_transaction_id_seq')
        , pi_creditor_account_id
        , CLOCK_TIMESTAMP()
        , pi_posted_by
        , pi_posted_by_name
        , pi_transaction_type
        , pi_imposition_allocation_amount
        , pi_result_id
        , FALSE
        , pi_payment_reference
        , NULL
        , NULL
        , 'impositions'::t_associated_record_type_enum
        , pi_imposition_id::TEXT
    );

EXCEPTION
    WHEN SQLSTATE 'P3801' OR SQLSTATE 'P3802' OR SQLSTATE 'P3803'
      OR SQLSTATE 'P3804' OR SQLSTATE 'P3805' OR SQLSTATE 'P3806'
      OR SQLSTATE 'P3807' OR SQLSTATE 'P3808' OR SQLSTATE 'P3809'
      OR SQLSTATE 'P3810' OR SQLSTATE 'P3811' THEN
        RAISE NOTICE 'Error in p_insert_creditor_transactions: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_insert_creditor_transactions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE;
END;
$procedure$;
