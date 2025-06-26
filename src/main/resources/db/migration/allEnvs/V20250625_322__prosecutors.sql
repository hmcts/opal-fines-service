/**
* OPAL Program
*
* MODULE      : prosecutors.sql
*
* DESCRIPTION : Create the PROSECUTORS table in the opal fines database.
*
* VERSION HISTORY:
*
* Date          Author        Version    Nature of Change
* ----------    ---------     -------    ----------------------------------------------------------------
* 25/06/2025    A Dennis      1.0        PO-1433 Create the PROSECUTORS table in the opal fines database
*
**/

CREATE TABLE prosecutors
(
 prosecutor_id          smallint           not null
,name                   varchar(200)       not null
,prosecutor_code        varchar(4)
,address_line_1         varchar(35)
,address_line_2         varchar(35)
,address_line_3         varchar(35)
,address_line_4         varchar(35)
,address_line_5         varchar(35)
,postcode               varchar(8)
,end_date               timestamp
,CONSTRAINT prosecutors_pk PRIMARY KEY (prosecutor_id)
);

COMMENT ON COLUMN prosecutors.prosecutor_id IS 'Unique ID of this record';
COMMENT ON COLUMN prosecutors.name IS 'Name of the designated prosecutor';
COMMENT ON COLUMN prosecutors.prosecutor_code IS 'Code of the designated prosecutor';
COMMENT ON COLUMN prosecutors.address_line_1 IS 'Address line 1';
COMMENT ON COLUMN prosecutors.address_line_2 IS 'Address line 2';
COMMENT ON COLUMN prosecutors.address_line_3 IS 'Address line 3';
COMMENT ON COLUMN prosecutors.address_line_4 IS 'Address line 4';
COMMENT ON COLUMN prosecutors.address_line_5 IS 'Address line 5';
COMMENT ON COLUMN prosecutors.end_date IS 'Last date that this designated prosecutor should be used within Opal';
