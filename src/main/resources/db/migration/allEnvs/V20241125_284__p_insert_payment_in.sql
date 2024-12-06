CREATE OR REPLACE PROCEDURE p_insert_payment_in(
    IN pi_till_id                   payments_in.till_id%TYPE,
    IN pi_payment_amount            payments_in.payment_amount%TYPE,
    IN pi_payment_method            payments_in.payment_method%TYPE,
    IN pi_destination_type          payments_in.destination_type%TYPE,
    IN pi_allocation_type           payments_in.allocation_type%TYPE DEFAULT NULL,
    IN pi_associated_record_type    payments_in.associated_record_type%TYPE DEFAULT NULL,
    IN pi_associated_record_id      payments_in.associated_record_id%TYPE DEFAULT NULL,
    IN pi_third_party_payer_name    payments_in.third_party_payer_name%TYPE DEFAULT NULL,
    IN pi_additional_information    payments_in.additional_information%TYPE DEFAULT NULL,
    IN pi_receipt                   payments_in.receipt%TYPE DEFAULT FALSE,
    IN pi_auto_payment              payments_in.auto_payment%TYPE DEFAULT FALSE)
AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_payment_in.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
BEGIN
    INSERT INTO payments_in (
                payment_in_id,
                till_id,
                payment_amount,
                payment_date,
                payment_method,
                destination_type,
                allocation_type,
                associated_record_type,
                associated_record_id,
                third_party_payer_name,
                additional_information,
                allocated,
                receipt,
                auto_payment)
    VALUES (
                nextval('payment_in_id_seq'),
                pi_till_id,
                pi_payment_amount,
                CURRENT_TIMESTAMP,
                pi_payment_method,
                pi_destination_type,
                pi_allocation_type,
                pi_associated_record_type,
                pi_associated_record_id,
                pi_third_party_payer_name,
                pi_additional_information,
                FALSE,
                pi_receipt,
                pi_auto_payment);
END;
$$ LANGUAGE plpgsql;
