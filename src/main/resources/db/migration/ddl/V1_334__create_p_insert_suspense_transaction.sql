DROP PROCEDURE IF EXISTS p_insert_suspense_transaction;

CREATE PROCEDURE p_insert_suspense_transaction(
    IN pi_suspense_item_id           suspense_transactions.suspense_item_id%TYPE,
    IN pi_posted_by                  suspense_transactions.posted_by%TYPE,
    IN pi_posted_by_name             suspense_transactions.posted_by_name%TYPE,
    IN pi_suspense_amount            suspense_transactions.amount%TYPE,
    IN pi_transaction_type           suspense_transactions.transaction_type%TYPE,
    IN pi_payment_in_id              payments_in.payment_in_id%TYPE,
    IN pi_additional_information     payments_in.additional_information%TYPE,
    IN pi_court_fees_received_id     court_fees_received.court_fee_received_id%TYPE
)
LANGUAGE plpgsql
AS $$
/**
* OPAL Program
*
* MODULE      : create_p_insert_suspense_transaction.sql
*
* DESCRIPTION : Create suspense_transactions records during till allocation.
*
* PARAMETERS  : pi_suspense_item_id       - Suspense item receiving the transaction
*               pi_posted_by              - User identifier posting the transaction
*               pi_posted_by_name         - User name posting the transaction
*               pi_suspense_amount        - Suspense transaction amount
*               pi_transaction_type       - Suspense transaction type code
*               pi_payment_in_id          - Related payment in identifier
*               pi_additional_information - Payment in additional information JSON/text
*               pi_court_fees_received_id - Court fee received record linked to the transaction, when applicable
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 01/10/2026    C Cho       1.0         PO-3474 - Create p_insert_suspense_transaction
* 
**/
DECLARE
    v_additional_information     JSONB;
    v_payment_information        JSONB;
    v_payer_details              JSONB;
    v_auto_payment               payments_in.auto_payment%TYPE;
    v_transaction_text           suspense_transactions.text%TYPE;
    v_suspense_transaction_id    suspense_transactions.suspense_transaction_id%TYPE;
BEGIN
    IF pi_suspense_item_id IS NULL THEN
        RAISE EXCEPTION 'pi_suspense_item_id must be provided'
            USING ERRCODE = 'P3309'
                , DETAIL = 'p_insert_suspense_transaction: pi_suspense_item_id is required';
    END IF;

    IF pi_suspense_amount IS NULL THEN
        RAISE EXCEPTION 'pi_suspense_amount must be provided'
            USING ERRCODE = 'P3310'
                , DETAIL = 'p_insert_suspense_transaction: pi_suspense_amount is required';
    END IF;

    IF pi_transaction_type IS NULL THEN
        RAISE EXCEPTION 'pi_transaction_type must be provided'
            USING ERRCODE = 'P3311'
                , DETAIL = 'p_insert_suspense_transaction: pi_transaction_type is required';
    END IF;

    BEGIN
        v_additional_information := NULLIF(pi_additional_information::TEXT, '')::JSONB;
    EXCEPTION
        WHEN OTHERS THEN
            v_additional_information := NULL;
    END;

    v_payment_information := COALESCE(
        v_additional_information -> 'additional_information', v_additional_information
    );
    v_payer_details := v_payment_information -> 'payer_details';

    SELECT COALESCE(p.auto_payment, FALSE)
      INTO v_auto_payment
      FROM payments_in p
     WHERE p.payment_in_id = pi_payment_in_id;

    IF COALESCE(v_auto_payment, FALSE) THEN
        -- Unidentified Auto Payments In records use the full originator reference.
        v_transaction_text := COALESCE(
            NULLIF(v_payment_information ->> 'originator_reference', ''),
            NULLIF(v_additional_information ->> 'originator_reference', ''),
            NULLIF(pi_additional_information::TEXT, '')
        );
    ELSE
        -- Manual suspense and court-fee payments preserve the full payment and payer details.
        v_transaction_text := NULLIF(
            CONCAT_WS(', ',
                NULLIF(v_payment_information ->> 'payment_reference', ''),
                NULLIF(v_payment_information ->> 'transfer_source', ''),
                NULLIF(v_payment_information ->> 'suspense_reason_detail', ''),
                NULLIF(v_payer_details -> 'individual' ->> 'title', ''),
                NULLIF(v_payer_details -> 'individual' ->> 'forenames', ''),
                NULLIF(v_payer_details -> 'individual' ->> 'surname', ''),
                NULLIF(v_payer_details -> 'organisation' ->> 'organisation_name', ''),
                NULLIF(v_payer_details ->> 'address_line_1', ''),
                NULLIF(v_payer_details ->> 'address_line_2', ''),
                NULLIF(v_payer_details ->> 'address_line_3', ''),
                NULLIF(v_payer_details ->> 'postcode', '')
            ),
            ''
        );
    END IF;

    INSERT INTO suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        posted_by,
        posted_by_name,
        transaction_type,
        amount,
        associated_record_type,
        associated_record_id,
        text,
        reversed
    )
    VALUES (
        nextval('suspense_transaction_id_seq'),
        pi_suspense_item_id,
        CURRENT_TIMESTAMP,
        pi_posted_by,
        pi_posted_by_name,
        pi_transaction_type,
        pi_suspense_amount,
        NULL,
        pi_payment_in_id::TEXT,
        v_transaction_text,
        NULL
    )
    RETURNING suspense_transaction_id
         INTO v_suspense_transaction_id;

    IF pi_court_fees_received_id IS NOT NULL THEN
        UPDATE court_fees_received
           SET suspense_transaction_id = v_suspense_transaction_id
         WHERE court_fee_received_id = pi_court_fees_received_id;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'court_fees_received record % was not found', pi_court_fees_received_id
                USING ERRCODE = 'P3312'
                    , DETAIL = 'p_insert_suspense_transaction: No court_fees_received record exists for '
                        || 'court_fee_received_id ' || pi_court_fees_received_id;
        END IF;
    END IF;
END;
$$;
