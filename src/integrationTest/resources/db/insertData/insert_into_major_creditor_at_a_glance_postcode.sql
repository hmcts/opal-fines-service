/**
* OPAL Program
*
* MODULE      : insert_into_major_creditor_at_a_glance_postcode.sql
*
* DESCRIPTION : Ensures the seeded MJ at-a-glance test account has a postcode for positive postcode mapping checks.
*/

UPDATE public.major_creditors
SET postcode = 'MC1 1AA'
WHERE major_creditor_id = 770000000041;
