/**
* CGI OPAL Program
*
* MODULE      : amend_alternate_payment_references_for_ei10.sql
*
* DESCRIPTION : Amend the alternate_payment_references table for APR Management EI10.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 21/10/2026    T Tang         1.0         PO-10851 - Amend alternate_payment_references for APR Management EI10.
*
**/

-- PO-10809 must be applied first so that business_unit_id is available.

ALTER TABLE public.alternate_payment_references
    ADD COLUMN version_number bigint NULL;

ALTER TABLE public.alternate_payment_references
    ALTER COLUMN apr_text TYPE varchar(18);

ALTER TABLE public.alternate_payment_references
    ADD CONSTRAINT apr_business_unit_id_apr_text_uk
        UNIQUE (business_unit_id, apr_text);

CREATE INDEX apr_apr_text_idx
    ON public.alternate_payment_references
    USING btree (apr_text);

COMMENT ON COLUMN public.alternate_payment_references.version_number IS 'Used to check that related items have not changed since retrieval and prior to being amended.';
