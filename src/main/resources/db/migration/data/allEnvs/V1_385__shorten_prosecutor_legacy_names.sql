/**
* OPAL Program
*
* MODULE      : shorten_prosecutor_legacy_names.sql
*
* DESCRIPTION : Apply approved legacy prosecutor names within the GOB 40-character limit.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 20/11/2026    P Brumby       1.0         PO-11411 - Shorten specified legacy prosecutor names
*
**/

DO $migration$
DECLARE
    r record;
    v_name text;
BEGIN
    FOR r IN
        SELECT *
        FROM (VALUES
            (1, 'Met Camera Processing Services / Traffic Offence Reports',
                'Met Police Traffic Offence Reports'),
            (7, 'Northumbria Criminal Justice Fixed Penalty Unit',
                'Northumbria Criminal Justice FP Unit'),
            (10, 'West Yorkshire Prosecutions and Casualty Prevention Unit',
                 'West Yorkshire Casualty Prevention Unit'),
            (36, 'Avon and Somerset Road Safety Support Unit',
                 'Avon & Somerset Road Safety Support Unit'),
            (44, 'British Transport Police Justice Department',
                 'BTP Justice Department')
        ) AS changes(id, old_name, new_name)
        ORDER BY id
    LOOP
        SELECT name INTO v_name
        FROM prosecutors_legacy
        WHERE prosecutor_id = r.id
        FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION
                'PO-11411: prosecutors_legacy ID % is missing', r.id;
        END IF;

        IF v_name IS DISTINCT FROM r.old_name
           AND v_name IS DISTINCT FROM r.new_name THEN
            RAISE EXCEPTION
                'PO-11411: prosecutors_legacy ID % has unexpected name: %',
                r.id, v_name;
        END IF;

        UPDATE prosecutors_legacy
        SET name = r.new_name
        WHERE prosecutor_id = r.id
          AND name IS DISTINCT FROM r.new_name;
    END LOOP;
END;
$migration$;
