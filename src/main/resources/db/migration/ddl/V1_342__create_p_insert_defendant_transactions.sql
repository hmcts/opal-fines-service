CREATE OR REPLACE PROCEDURE public.p_insert_defendant_transactions(
    IN  pi_defendant_account_id       defendant_transactions.defendant_account_id%TYPE,
    IN  pi_posted_by                 defendant_transactions.posted_by%TYPE,
    IN  pi_posted_by_name            defendant_transactions.posted_by_name%TYPE,
    IN  pi_transaction_type          defendant_transactions.transaction_type%TYPE,
    IN  pi_payment_amount            defendant_transactions.transaction_amount%TYPE,
    IN  pi_payment_method            defendant_transactions.payment_method%TYPE,
    IN  pi_payment_reference         defendant_transactions.payment_reference%TYPE,
    IN  pi_third_party_payer_name    defendant_transactions.text%TYPE,
    IN  pi_associated_record_id      defendant_transactions.associated_record_id%TYPE,
    IN  pi_source                    defendant_transactions.source%TYPE,
    OUT po_defendant_transaction_id  defendant_transactions.defendant_transaction_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_insert_defendant_transactions.sql
*
* DESCRIPTION : Insert a defendant transaction during till allocation.
*
* PARAMETERS  : pi_defendant_account_id      - Defendant account receiving the allocated payment
*               pi_posted_by                 - User identifier posting the transaction
*               pi_posted_by_name            - Name of the user posting the transaction
*               pi_transaction_type          - Defendant transaction type
*               pi_payment_amount            - Amount allocated to the defendant account
*               pi_payment_method            - Method used for the payment
*               pi_payment_reference         - Payment reference supplied for the payment
*               pi_third_party_payer_name    - Third-party payer name stored as transaction text
*               pi_associated_record_id      - Payments In record identifier associated with the transaction
*               pi_source                    - Party that sent the associated payment
*               po_defendant_transaction_id - Identifier of the created defendant transaction
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 13/10/2026    C Cho          1.0         PO-3446 - Create p_insert_defendant_transactions
*
**/
DECLARE
    v_pg_exception_detail TEXT;
BEGIN
    IF pi_defendant_account_id IS NULL THEN
        RAISE EXCEPTION 'Defendant account ID cannot be null'
            USING ERRCODE = 'P3401',
                  DETAIL = 'p_insert_defendant_transactions: pi_defendant_account_id is required';
    END IF;

    IF pi_transaction_type IS NULL THEN
        RAISE EXCEPTION 'Transaction type cannot be null'
            USING ERRCODE = 'P3402',
                  DETAIL = 'p_insert_defendant_transactions: pi_transaction_type is required';
    END IF;

    IF pi_payment_amount IS NULL THEN
        RAISE EXCEPTION 'Payment amount cannot be null'
            USING ERRCODE = 'P3403',
                  DETAIL = 'p_insert_defendant_transactions: pi_payment_amount is required';
    END IF;

    IF pi_payment_amount <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be greater than zero'
            USING ERRCODE = 'P3404',
                  DETAIL = 'p_insert_defendant_transactions: pi_payment_amount must be a positive value';
    END IF;

    IF pi_payment_method IS NULL THEN
        RAISE EXCEPTION 'Payment method cannot be null'
            USING ERRCODE = 'P3405',
                  DETAIL = 'p_insert_defendant_transactions: pi_payment_method is required';
    END IF;

    IF NULLIF(BTRIM(pi_associated_record_id), '') IS NULL THEN
        RAISE EXCEPTION 'Associated record ID cannot be null or blank'
            USING ERRCODE = 'P3406',
                  DETAIL = 'p_insert_defendant_transactions: pi_associated_record_id is required';
    END IF;

    IF NULLIF(BTRIM(pi_source), '') IS NULL THEN
        RAISE EXCEPTION 'Source cannot be null or blank'
            USING ERRCODE = 'P3407',
                  DETAIL = 'p_insert_defendant_transactions: pi_source is required';
    END IF;

    INSERT INTO public.defendant_transactions (
          defendant_transaction_id
        , defendant_account_id
        , posted_date
        , posted_by
        , posted_by_name
        , transaction_type
        , transaction_amount
        , payment_method
        , payment_reference
        , "text"
        , status
        , status_date
        , associated_record_type
        , associated_record_id
        , source
    )
    VALUES (
          NEXTVAL('defendant_transaction_id_seq')
        , pi_defendant_account_id
        , CLOCK_TIMESTAMP()
        , pi_posted_by
        , pi_posted_by_name
        , pi_transaction_type
        , pi_payment_amount
        , pi_payment_method
        , pi_payment_reference
        , pi_third_party_payer_name
        , NULL
        , CLOCK_TIMESTAMP()
        , 'payments_in'::t_associated_record_type_enum
        , pi_associated_record_id
        , pi_source
    )
    RETURNING defendant_transaction_id
         INTO po_defendant_transaction_id;

EXCEPTION
    WHEN SQLSTATE 'P3401' OR SQLSTATE 'P3402' OR SQLSTATE 'P3403'
      OR SQLSTATE 'P3404' OR SQLSTATE 'P3405' OR SQLSTATE 'P3406'
      OR SQLSTATE 'P3407' THEN
        RAISE NOTICE 'Error in p_insert_defendant_transactions: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_insert_defendant_transactions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_insert_defendant_transactions: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$procedure$;
