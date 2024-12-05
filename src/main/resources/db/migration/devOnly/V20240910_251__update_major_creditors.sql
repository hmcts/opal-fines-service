/**
* CGI OPAL Program
*
* MODULE      : update_major_creditors.sql
*
* DESCRIPTION : Update major_creditors table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 10/09/2024    A Dennis    1.0         PO-755 Update major_creditors table to use their parent business unit ids as a result of Business Units reference data loaded from Legacy GoB system test Oracle database.
*                                       Note that the UPDATE statements have been arranged in order so that an UPDATE would not be overwritten by another UPDATE further down.
*
**/
UPDATE major_creditors
SET   business_unit_id = 8
WHERE business_unit_id = 47;

UPDATE major_creditors
SET   business_unit_id = 47
WHERE business_unit_id = 43;

UPDATE major_creditors
SET   business_unit_id = 12
WHERE business_unit_id = 52;

UPDATE major_creditors
SET   business_unit_id = 52
WHERE business_unit_id = 44;

UPDATE major_creditors
SET   business_unit_id = 44
WHERE business_unit_id = 72;

UPDATE major_creditors
SET   business_unit_id = 5
WHERE business_unit_id = 45;

UPDATE major_creditors
SET   business_unit_id = 45
WHERE business_unit_id = 79;

UPDATE major_creditors
SET   business_unit_id = 139
WHERE business_unit_id = 46;

UPDATE major_creditors
SET   business_unit_id = 9
WHERE business_unit_id = 48;

UPDATE major_creditors
SET   business_unit_id = 10
WHERE business_unit_id = 49;

UPDATE major_creditors
SET   business_unit_id = 112
WHERE business_unit_id = 82;

UPDATE major_creditors
SET   business_unit_id = 82
WHERE business_unit_id = 57;

UPDATE major_creditors
SET   business_unit_id = 57
WHERE business_unit_id = 50;

UPDATE major_creditors
SET   business_unit_id = 11
WHERE business_unit_id = 51;

UPDATE major_creditors
SET   business_unit_id = 26
WHERE business_unit_id = 60;

UPDATE major_creditors
SET   business_unit_id = 60
WHERE business_unit_id = 53;

UPDATE major_creditors
SET   business_unit_id = 116
WHERE business_unit_id = 54;

UPDATE major_creditors
SET   business_unit_id = 28
WHERE business_unit_id = 80;

UPDATE major_creditors
SET   business_unit_id = 80
WHERE business_unit_id = 61;

UPDATE major_creditors
SET   business_unit_id = 61
WHERE business_unit_id = 55;

UPDATE major_creditors
SET   business_unit_id = 14
WHERE business_unit_id = 56;

UPDATE major_creditors
SET   business_unit_id = 24
WHERE business_unit_id = 89;

UPDATE major_creditors
SET   business_unit_id = 89
WHERE business_unit_id = 58;

UPDATE major_creditors
SET   business_unit_id = 31
WHERE business_unit_id = 92;

UPDATE major_creditors
SET   business_unit_id = 92
WHERE business_unit_id = 59;

UPDATE major_creditors
SET   business_unit_id = 130
WHERE business_unit_id = 96;

UPDATE major_creditors
SET   business_unit_id = 96
WHERE business_unit_id = 62;

UPDATE major_creditors
SET   business_unit_id = 138
WHERE business_unit_id = 99;

UPDATE major_creditors
SET   business_unit_id = 99
WHERE business_unit_id = 63;

UPDATE major_creditors
SET   business_unit_id = 103
WHERE business_unit_id = 64;

UPDATE major_creditors
SET   business_unit_id = 21
WHERE business_unit_id = 65;

UPDATE major_creditors
SET   business_unit_id = 65
WHERE business_unit_id = 70;

UPDATE major_creditors
SET   business_unit_id = 22
WHERE business_unit_id = 66;

UPDATE major_creditors
SET   business_unit_id = 66
WHERE business_unit_id = 68;

UPDATE major_creditors
SET   business_unit_id = 109
WHERE business_unit_id = 77;

UPDATE major_creditors
SET   business_unit_id = 77
WHERE business_unit_id = 67;

UPDATE major_creditors
SET   business_unit_id = 67
WHERE business_unit_id = 73;

UPDATE major_creditors
SET   business_unit_id = 73
WHERE business_unit_id = 71;

UPDATE major_creditors
SET   business_unit_id = 71
WHERE business_unit_id = 100;

UPDATE major_creditors
SET   business_unit_id = 106
WHERE business_unit_id = 78;

UPDATE major_creditors
SET   business_unit_id = 78
WHERE business_unit_id = 69;

UPDATE major_creditors
SET   business_unit_id = 111
WHERE business_unit_id = 74;

UPDATE major_creditors
SET   business_unit_id = 105
WHERE business_unit_id = 75;

UPDATE major_creditors
SET   business_unit_id = 30
WHERE business_unit_id = 76;

UPDATE major_creditors
SET   business_unit_id = 29
WHERE business_unit_id = 81;

UPDATE major_creditors
SET   business_unit_id = 119
WHERE business_unit_id = 83;

UPDATE major_creditors
SET   business_unit_id = 113
WHERE business_unit_id = 97;

UPDATE major_creditors
SET   business_unit_id = 97
WHERE business_unit_id = 84;

UPDATE major_creditors
SET   business_unit_id = 36
WHERE business_unit_id = 85;

UPDATE major_creditors
SET   business_unit_id = 110
WHERE business_unit_id = 86;

UPDATE major_creditors
SET   business_unit_id = 124
WHERE business_unit_id = 87;

UPDATE major_creditors
SET   business_unit_id = 38
WHERE business_unit_id = 88;

UPDATE major_creditors
SET   business_unit_id = 125
WHERE business_unit_id = 90;

UPDATE major_creditors
SET   business_unit_id = 126
WHERE business_unit_id = 91;

UPDATE major_creditors
SET   business_unit_id = 107
WHERE business_unit_id = 93;

UPDATE major_creditors
SET   business_unit_id = 128
WHERE business_unit_id = 94;

UPDATE major_creditors
SET   business_unit_id = 129
WHERE business_unit_id = 95;

UPDATE major_creditors
SET   business_unit_id = 135
WHERE business_unit_id = 98;
