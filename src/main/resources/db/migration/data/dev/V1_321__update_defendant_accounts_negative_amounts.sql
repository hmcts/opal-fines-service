/**
* OPAL Program
*
* MODULE      : update_defendant_accounts_negative_amounts.sql
*
* DESCRIPTION : Update defendant account amount imposed and account balance values
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 01/09/2026    C Cho       1.0         PO-10248 Update defendant account amount imposed and account balance values
*
**/

UPDATE defendant_accounts
   SET amount_imposed = -ABS(amount_imposed),
       account_balance = -ABS(amount_imposed) + amount_paid
 WHERE amount_imposed > 0
    OR account_balance <> -ABS(amount_imposed) + amount_paid;
