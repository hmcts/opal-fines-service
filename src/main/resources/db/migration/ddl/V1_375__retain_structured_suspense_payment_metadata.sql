-- PO-11365: retain allocation metadata in durable structured data, independently of Payment In JSON.
ALTER TABLE suspense_transactions ADD COLUMN additional_information JSONB;
COMMENT ON COLUMN suspense_transactions.additional_information
    IS 'Structured payment metadata for new allocations; text remains the legacy ETL source.';

CREATE OR REPLACE PROCEDURE p_insert_suspense_transaction(
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
DECLARE
    v_additional_information     JSONB;
    v_payment_information        JSONB;
    v_metadata                   JSONB;
    v_associated_record_type     payments_in.associated_record_type%TYPE;
    v_associated_record_id       TEXT;
    v_third_party_payer_name     payments_in.third_party_payer_name%TYPE;
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
    -- Keep payer identity in Party records. Only allocation metadata survives in this JSON.
    SELECT CASE WHEN NOT COALESCE(p.auto_payment, FALSE) THEN p.associated_record_type END,
           CASE WHEN NOT COALESCE(p.auto_payment, FALSE) AND p.associated_record_type IS NOT NULL
                THEN p.associated_record_id ELSE pi_payment_in_id::TEXT END,
           p.third_party_payer_name
      INTO v_associated_record_type, v_associated_record_id, v_third_party_payer_name
      FROM payments_in p
     WHERE p.payment_in_id = pi_payment_in_id;

    v_metadata := JSONB_STRIP_NULLS(JSONB_BUILD_OBJECT(
        'payment_received_from', COALESCE(v_payment_information -> 'payment_received_from',
                                         v_additional_information -> 'payment_received_from'),
        'payment_reference', COALESCE(v_payment_information -> 'payment_reference',
                                     v_payment_information -> 'originator_reference',
                                     v_additional_information -> 'originator_reference',
                                     CASE WHEN JSONB_TYPEOF(v_additional_information) = 'string'
                                          THEN v_additional_information END),
        'transfer_source', COALESCE(v_payment_information -> 'transfer_source', v_payment_information -> 'source'),
        'suspense_reason_code', v_payment_information -> 'suspense_reason_code',
        'suspense_reason_detail', v_payment_information -> 'suspense_reason_detail',
        'third_party_payer_name', v_third_party_payer_name
    ));

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
        additional_information,
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
        v_associated_record_type,
        v_associated_record_id,
        NULL,
        v_metadata,
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

-- Court Fee overpayments reuse the item linked to the same durable account.
CREATE OR REPLACE PROCEDURE p_allocate_suspense_payment(
    IN pi_posted_by                  suspense_transactions.posted_by%TYPE,
    IN pi_posted_by_name             suspense_transactions.posted_by_name%TYPE,
    IN pi_business_unit_id           business_units.business_unit_id%TYPE,
    IN pi_suspense_amount            suspense_transactions.amount%TYPE,
    IN pi_destination_type           payments_in.destination_type%TYPE,
    IN pi_suspense_item_type         suspense_items.suspense_item_type%TYPE,
    IN pi_payment_method             suspense_items.payment_method%TYPE,
    IN pi_payment_in_id              payments_in.payment_in_id%TYPE,
    IN pi_additional_information     payments_in.additional_information%TYPE,
    IN pi_court_fee_received_id      court_fees_received.court_fee_received_id%TYPE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_auto_payment               payments_in.auto_payment%TYPE;
    v_allocation_function        VARCHAR(30);
    v_payment_information        JSONB;
    v_suspense_item_type         suspense_items.suspense_item_type%TYPE;
    v_transaction_type           suspense_transactions.transaction_type%TYPE;
    v_suspense_item_id           suspense_items.suspense_item_id%TYPE;
    v_new_suspense_item          BOOLEAN := FALSE;
    v_suspense_note              notes.note_text%TYPE;
    v_overpayment                court_fees_received.overpayment%TYPE;
    v_court_fee_business_unit_id court_fees_received.business_unit_id%TYPE;
BEGIN
    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'pi_business_unit_id must be provided'
            USING ERRCODE = 'P3461'
                , DETAIL = 'p_allocate_suspense_payment: pi_business_unit_id is required';
    END IF;

    IF pi_suspense_amount IS NULL OR pi_suspense_amount <= 0 THEN
        RAISE EXCEPTION 'pi_suspense_amount must be greater than zero'
            USING ERRCODE = 'P3462'
                , DETAIL = FORMAT('p_allocate_suspense_payment: Passed pi_suspense_amount: %s',
                    COALESCE(pi_suspense_amount::TEXT, 'NULL'));
    END IF;

    IF pi_destination_type IS NULL OR pi_destination_type::TEXT NOT IN ('C', 'S', 'F') THEN
        RAISE EXCEPTION 'pi_destination_type must be C, S or F'
            USING ERRCODE = 'P3463'
                , DETAIL = FORMAT('p_allocate_suspense_payment: Passed pi_destination_type: %s; expected C, S or F',
                    COALESCE(pi_destination_type::TEXT, 'NULL'));
    END IF;

    SELECT auto_payment
      INTO v_auto_payment
      FROM payments_in
     WHERE payment_in_id = pi_payment_in_id
       FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'payments_in record % was not found', pi_payment_in_id
            USING ERRCODE = 'P3464'
                , DETAIL = FORMAT('p_allocate_suspense_payment: No payments_in record exists for pi_payment_in_id: %s',
                    COALESCE(pi_payment_in_id::TEXT, 'NULL'));
    END IF;

    v_allocation_function := CASE
        WHEN COALESCE(v_auto_payment, FALSE) THEN 'Auto Payments In'
        ELSE 'Manual Cash Input'
    END;

    BEGIN
        v_payment_information := NULLIF(pi_additional_information::TEXT, '')::JSONB;
    EXCEPTION
        WHEN invalid_text_representation THEN
            v_payment_information := NULL;
    END;
    v_payment_information := COALESCE(
        v_payment_information -> 'additional_information', v_payment_information
    );

    IF pi_destination_type::TEXT = 'C' AND pi_court_fee_received_id IS NULL THEN
        RAISE EXCEPTION 'pi_court_fee_received_id must be provided for Court Fee allocations'
            USING ERRCODE = 'P3468'
                , DETAIL = 'p_allocate_suspense_payment: pi_destination_type is C; '
                    || 'pi_court_fee_received_id is required to determine the overpayment flag';
    END IF;

    IF pi_court_fee_received_id IS NOT NULL THEN
        IF pi_destination_type::TEXT <> 'C' THEN
            RAISE EXCEPTION 'pi_court_fee_received_id is only applicable to Court Fee allocations'
                USING ERRCODE = 'P3465'
                    , DETAIL = FORMAT('p_allocate_suspense_payment: Passed pi_court_fee_received_id: %s, '
                        || 'pi_destination_type: %s; court fee received IDs require destination C',
                        pi_court_fee_received_id, pi_destination_type);
        END IF;

        SELECT overpayment, business_unit_id
          INTO v_overpayment, v_court_fee_business_unit_id
          FROM court_fees_received
         WHERE court_fee_received_id = pi_court_fee_received_id;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'court_fees_received record % was not found', pi_court_fee_received_id
                USING ERRCODE = 'P3466'
                    , DETAIL = FORMAT('p_allocate_suspense_payment: No court_fees_received record exists '
                        || 'for pi_court_fee_received_id: %s', pi_court_fee_received_id);
        END IF;

        IF v_court_fee_business_unit_id <> pi_business_unit_id THEN
            RAISE EXCEPTION 'court_fees_received record % belongs to a different business unit',
                pi_court_fee_received_id
                USING ERRCODE = 'P3467'
                    , DETAIL = FORMAT('p_allocate_suspense_payment: Court fee received ID: %s belongs '
                        || 'to business unit: %s; passed pi_business_unit_id: %s',
                        pi_court_fee_received_id, v_court_fee_business_unit_id, pi_business_unit_id);
        END IF;
    END IF;

    CASE pi_destination_type::TEXT
        WHEN 'C' THEN
            v_suspense_item_type := CASE
                WHEN v_overpayment THEN 'OC'::t_suspense_item_type_enum
                ELSE 'CF'::t_suspense_item_type_enum
            END;
        WHEN 'F' THEN
            v_suspense_item_type := CASE
                WHEN pi_suspense_item_type::TEXT = 'FA' THEN 'FA'::t_suspense_item_type_enum
                ELSE 'OF'::t_suspense_item_type_enum
            END;
        WHEN 'S' THEN
            IF v_allocation_function = 'Manual Cash Input' THEN
                -- Manual suspense items take their type from the payment's suspense reason.
                v_suspense_item_type := COALESCE(
                    NULLIF(v_payment_information ->> 'suspense_reason_code', '')::t_suspense_item_type_enum,
                    pi_suspense_item_type,
                    'UN'::t_suspense_item_type_enum
                );
            ELSE
                v_suspense_item_type := COALESCE(pi_suspense_item_type, 'UN'::t_suspense_item_type_enum);
            END IF;
    END CASE;

    v_transaction_type := CASE
        WHEN v_suspense_item_type::TEXT IN ('CF', 'OC', 'OF', 'FA')
            THEN v_suspense_item_type::TEXT::t_suspense_transaction_type_enum
        ELSE 'UN'::t_suspense_transaction_type_enum
    END;

    IF pi_destination_type::TEXT = 'C' AND v_overpayment THEN
        -- For court fee overpayment transaction, retrieve the suspense item created by the first non-overpayment court fee transaction
        SELECT st.suspense_item_id
          INTO STRICT v_suspense_item_id
          FROM suspense_transactions st
          JOIN suspense_items si ON si.suspense_item_id = st.suspense_item_id
          JOIN suspense_accounts sa ON sa.suspense_account_id = si.suspense_account_id
          JOIN payments_in pi ON pi.payment_in_id = pi_payment_in_id
         WHERE ((st.associated_record_type = pi.associated_record_type
                 AND st.associated_record_id = pi.associated_record_id)
                OR (st.associated_record_type IS NULL AND st.associated_record_id = pi_payment_in_id::TEXT))
           AND st.transaction_type::TEXT = 'CF'
           AND st.reversed IS NULL
           AND si.suspense_item_type::TEXT = 'CF'
           AND sa.business_unit_id = pi_business_unit_id
         ORDER BY st.suspense_transaction_id
         LIMIT 1;
    ELSE
        CALL p_insert_suspense_item(
            pi_suspense_item_type := v_suspense_item_type,
            pi_payment_method := pi_payment_method,
            pi_business_unit_id := pi_business_unit_id,
            pi_court_fee_received_id := pi_court_fee_received_id,
            po_suspense_item_id := v_suspense_item_id
        );
        v_new_suspense_item := TRUE;
    END IF;

    CALL p_insert_suspense_transaction(
        pi_suspense_item_id := v_suspense_item_id,
        pi_posted_by := pi_posted_by,
        pi_posted_by_name := pi_posted_by_name,
        pi_suspense_amount := pi_suspense_amount,
        pi_transaction_type := v_transaction_type,
        pi_payment_in_id := pi_payment_in_id,
        pi_additional_information := pi_additional_information,
        pi_court_fees_received_id := pi_court_fee_received_id
    );

    IF v_allocation_function = 'Manual Cash Input' AND v_new_suspense_item THEN
        v_suspense_note := v_payment_information ->> 'suspense_note';

        IF NULLIF(BTRIM(v_suspense_note), '') IS NOT NULL THEN
            CALL p_insert_note(
                pi_associated_record_type := 'suspense_items'::t_associated_record_type_enum,
                pi_associated_record_id := v_suspense_item_id::VARCHAR,
                pi_note_text := v_suspense_note,
                pi_posted_by := pi_posted_by,
                pi_posted_by_name := pi_posted_by_name
            );
        END IF;
    END IF;
END;
$$;
