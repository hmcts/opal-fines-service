/**
* OPAL Program
*
* MODULE      : alter_payment_card_requests_for_allpay_out.sql
*
* DESCRIPTION : Add interface_job_id column to payment_card_requests for Allpay Out
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    -------------------------------------------------------------------------
* 11/11/2026    TMc            1.0         PO-11165 - Alter PAYMENT_CARD_REQUESTS table and related Stored Procedure
*
**/

ALTER TABLE payment_card_requests
    ADD COLUMN interface_job_id BIGINT;

COMMENT ON COLUMN payment_card_requests.interface_job_id IS 'ID of the Interface Job that processed this payment card request';

ALTER TABLE payment_card_requests
    ADD CONSTRAINT pcr_interface_job_id_fk
        FOREIGN KEY (interface_job_id)
        REFERENCES interface_jobs (interface_job_id);

CREATE INDEX pcr_interface_job_id_idx
    ON payment_card_requests (interface_job_id);
