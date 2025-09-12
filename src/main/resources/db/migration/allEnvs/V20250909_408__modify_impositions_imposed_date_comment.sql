/**
* OPAL Program
*
* MODULE      : modify_impositions_imposed_date_comment.sql
*
* DESCRIPTION : Change the database comment on IMPOSITIONS.IMPOSED_DATE column.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 04/09/2025    P Brumby    1.0         PO-1845 Change the database comment on IMPOSITIONS.IMPOSED_DATE column to 'The date this financial penalty was imposed in a court hearing, or the Date of Offence for FP Tickets'.
*
**/

COMMENT ON COLUMN impositions.imposed_date IS 'The date this financial penalty was imposed in a court hearing, or the Date of Offence for FP Tickets';