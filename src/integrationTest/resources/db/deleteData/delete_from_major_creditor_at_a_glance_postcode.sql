/**
* OPAL Program
*
* MODULE      : delete_from_major_creditor_at_a_glance_postcode.sql
*
* DESCRIPTION : Restores the seeded MJ at-a-glance test account postcode after postcode mapping checks.
*/

UPDATE public.major_creditors
SET postcode = NULL
WHERE major_creditor_id = 770000000041;
