CREATE OR REPLACE PROCEDURE p_allocate_payments(
    IN pi_defendant_account_id      defendant_accounts.defendant_account_id%TYPE,
    IN pi_posted_by                 creditor_transactions.posted_by%TYPE,
    IN pi_posted_by_name            creditor_transactions.posted_by_name%TYPE,
    IN pi_payment_amount            payments_in.payment_amount%TYPE,
    IN pi_transaction_type          allocations.transaction_type%TYPE,
    IN pi_allocation_function       allocations.allocation_function%TYPE,
    IN pi_payment_reference         creditor_transactions.payment_reference%TYPE,
    IN pi_defendant_transaction_id  defendant_transactions.defendant_transaction_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_allocate_payments.sql
*
* DESCRIPTION : Allocate a payment proportionally across priority 1 impositions,
*               then sequentially across priorities 2-9.
*
* PARAMETERS  : pi_defendant_account_id     - Defendant account receiving the payment
*               pi_posted_by                - User identifier posting the creditor transaction
*               pi_posted_by_name           - Name of the user posting the creditor transaction
*               pi_payment_amount          - Positive payment amount
*               pi_transaction_type        - PAYMNT transaction type
*               pi_allocation_function     - Function responsible for the allocation
*               pi_payment_reference       - Payment reference for creditor transactions
*               pi_defendant_transaction_id - Existing defendant transaction belonging to the account
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 22/10/2026    C Cho       1.0         PO-3452 Create p_allocate_payments
*
**/
DECLARE
    rec                         RECORD;
    v_allocation_amount         allocations.allocated_amount%TYPE;
    v_remaining_payment_amount  payments_in.payment_amount%TYPE;
    v_creditor_transaction_type creditor_transactions.transaction_type%TYPE;
    v_pg_exception_detail       TEXT;
BEGIN
    IF pi_defendant_account_id IS NULL THEN
        RAISE EXCEPTION 'Defendant account ID cannot be null'
            USING ERRCODE = 'P3901'
                , DETAIL = 'p_allocate_payments: pi_defendant_account_id is required';
    END IF;

    IF pi_payment_amount IS NULL
       OR pi_payment_amount <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be greater than zero'
            USING ERRCODE = 'P3902'
                , DETAIL = 'p_allocate_payments: pi_payment_amount must be a positive value';
    END IF;

    IF COALESCE(pi_transaction_type::TEXT, '') <> 'PAYMNT' THEN
        RAISE EXCEPTION 'Transaction type must be PAYMNT'
            USING ERRCODE = 'P3903'
                , DETAIL = 'p_allocate_payments: pi_transaction_type must map to both allocation and creditor transaction type PAYMNT';
    END IF;

    IF NULLIF(BTRIM(pi_allocation_function), '') IS NULL THEN
        RAISE EXCEPTION 'Allocation function cannot be null or blank'
            USING ERRCODE = 'P3904'
                , DETAIL = 'p_allocate_payments: pi_allocation_function is required';
    END IF;

    IF pi_defendant_transaction_id IS NULL THEN
        RAISE EXCEPTION 'Defendant transaction ID cannot be null'
            USING ERRCODE = 'P3905'
                , DETAIL = 'p_allocate_payments: pi_defendant_transaction_id is required';
    END IF;

    PERFORM 1
      FROM defendant_accounts
     WHERE defendant_account_id = pi_defendant_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Defendant account % was not found', pi_defendant_account_id
            USING ERRCODE = 'P3906'
                , DETAIL = 'p_allocate_payments: no defendant account exists for the passed pi_defendant_account_id';
    END IF;

    PERFORM 1
      FROM defendant_transactions
     WHERE defendant_transaction_id = pi_defendant_transaction_id
       AND defendant_account_id = pi_defendant_account_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Defendant transaction % was not found for defendant account %',
                        pi_defendant_transaction_id,
                        pi_defendant_account_id
            USING ERRCODE = 'P3907'
                , DETAIL = 'p_allocate_payments: the defendant transaction must exist and belong to the passed defendant account';
    END IF;

    v_creditor_transaction_type := pi_transaction_type::TEXT::t_creditor_transaction_type_enum;

    -- Lock before calculating balances
    PERFORM i.imposition_id
    FROM impositions i
     WHERE i.defendant_account_id = pi_defendant_account_id
     ORDER BY i.imposition_id
       FOR UPDATE;

    v_remaining_payment_amount := pi_payment_amount;

    FOR rec IN (
        WITH p1 AS (
            SELECT i.imposition_id,
                   i.creditor_account_id,
                   i.result_id,
                    -(i.imposed_amount + i.paid_amount) AS imposition_balance
                FROM impositions i
                JOIN results r ON r.result_id = i.result_id
             WHERE i.defendant_account_id = pi_defendant_account_id
               AND r.imposition_allocation_priority = 1
               AND i.imposed_amount + i.paid_amount < 0
        ), shares AS (
            -- Calculate exact proportional amounts for each imposition
            SELECT p1.*,
                   LEAST(pi_payment_amount, SUM(imposition_balance) OVER ()) AS target_amount,-- The total p1 balance or payment amount, whichever is smaller
                   LEAST(pi_payment_amount, SUM(imposition_balance) OVER ())
                       * imposition_balance / SUM(imposition_balance) OVER () AS exact_amount -- Calculates each row its proportional share
              FROM p1
        ), pennies AS (
            -- Distribute any remaining pennies to ensure the total allocation matches the target amount.
            SELECT shares.*,
                   TRUNC(exact_amount, 2) AS base_amount,
                   ROW_NUMBER() OVER (
                    -- Ranks rows with the largest leftover fractions first, so they can receive any remaining pennies. See example 3 in LLD
                       ORDER BY exact_amount - TRUNC(exact_amount, 2) DESC, imposition_id
                   ) AS remainder_rank
              FROM shares
        )
        SELECT imposition_id, creditor_account_id, result_id,
               base_amount + CASE
                   WHEN remainder_rank <= ROUND(
                                                (target_amount - SUM(base_amount) OVER ()) * 100-- Calculates how much money(in pennies) is still unallocated
                                                )
                       THEN 0.01
                   ELSE 0
               END AS allocation_amount
          FROM pennies
         ORDER BY imposition_id
    )
    LOOP
        v_allocation_amount := rec.allocation_amount;
        CONTINUE WHEN v_allocation_amount <= 0;

        CALL p_insert_allocations(
            rec.imposition_id,
            v_allocation_amount,
            pi_transaction_type,
            pi_allocation_function,
            pi_defendant_transaction_id
        );
        CALL p_update_impositions(
            rec.imposition_id,
            v_allocation_amount
        );
        CALL p_insert_creditor_transactions(
            rec.creditor_account_id,
            pi_posted_by,
            pi_posted_by_name,
            v_creditor_transaction_type,
            v_allocation_amount,
            rec.result_id,
            pi_payment_reference,
            rec.imposition_id
        );

        v_remaining_payment_amount := v_remaining_payment_amount - v_allocation_amount;
    END LOOP;

    IF v_remaining_payment_amount > 0 THEN
        FOR rec IN (
            SELECT i.imposition_id,
                   i.creditor_account_id,
                   i.result_id,
                    -(i.imposed_amount + i.paid_amount) AS imposition_balance
                FROM impositions i
                JOIN results r ON r.result_id = i.result_id
             WHERE i.defendant_account_id = pi_defendant_account_id
               AND r.imposition_allocation_priority BETWEEN 2 AND 9
               AND i.imposed_amount + i.paid_amount < 0
             ORDER BY r.imposition_allocation_priority, i.posted_date, i.imposition_id --higher-priority and older or lower-ID impositions are allocated first
        )
        LOOP
            v_allocation_amount := LEAST(rec.imposition_balance, v_remaining_payment_amount);

            CALL p_insert_allocations(
                rec.imposition_id,
                v_allocation_amount,
                pi_transaction_type,
                pi_allocation_function,
                pi_defendant_transaction_id
            );
            CALL p_update_impositions(
                rec.imposition_id,
                v_allocation_amount
            );
            CALL p_insert_creditor_transactions(
                rec.creditor_account_id,
                pi_posted_by,
                pi_posted_by_name,
                v_creditor_transaction_type,
                v_allocation_amount,
                rec.result_id,
                pi_payment_reference,
                rec.imposition_id
            );

            v_remaining_payment_amount := v_remaining_payment_amount - v_allocation_amount;
            EXIT WHEN v_remaining_payment_amount = 0;
        END LOOP;
    END IF;

EXCEPTION
    WHEN SQLSTATE 'P3901' OR SQLSTATE 'P3902' OR SQLSTATE 'P3903'
      OR SQLSTATE 'P3904' OR SQLSTATE 'P3905' OR SQLSTATE 'P3906'
      OR SQLSTATE 'P3907' THEN
        RAISE NOTICE 'Error in p_allocate_payments: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_allocate_payments: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE;
END;
$procedure$;

COMMENT ON PROCEDURE p_allocate_payments
    IS 'Allocate a payment across impositions by priority in priority order.';
