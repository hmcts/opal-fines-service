/**
* OPAL Program
*
* MODULE      : shorten_local_justice_area_legacy_names.sql
*
* DESCRIPTION : Apply approved legacy LJA names within the GOB 40-character limit.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 20/11/2026    P Brumby       1.0         PO-11411 - Shorten specified legacy LJA names
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
            (463, 'Warwick Crown Court sitting at Leamington Spa',
                  'Warwick Crown sitting at Leamington Spa'),
            (1585, 'County Durham and Darlington Magistrates'' Court',
                   'County Durham & Darlington Mags'' Court'),
            (1752, 'North and West Cumbria Magistrates'' Court',
                   'North & West Cumbria Magistrates'' Court'),
            (1910, 'West and Central Hertfordshire Magistrates'' Court',
                   'West & Central Herts Magistrates'' Court'),
            (1911, 'North and East Hertfordshire Magistrates'' Court',
                   'North & East Herts Magistrates'' Court'),
            (2905, 'Coventry and Warwickshire (Leamington) Magistrates'' Court',
                   'Coventry & Warws (Leamington) Mags Court'),
            (2922, 'Birmingham and Solihull Magistrates'' Court',
                   'Birmingham & Solihull Magistrates'' Court'),
            (2924, 'Coventry and Warwickshire (Coventry) Magistrates'' Court',
                   'Coventry & Warws (Coventry) Mags Court'),
            (3246, 'Mid Wales (Llandrindod Wells) Magistrates'' Court',
                   'Mid Wales (Llandrindod Wells) Mags Court'),
            (3253, 'Ceredigion and Pembrokeshire Magistrates'' Court',
                   'Ceredigion & Pembrokeshire Mags'' Court'),
            (3260, 'Mid Wales (Merthyr Tydfil) Magistrates'' Court',
                   'Mid Wales (Merthyr Tydfil) Mags'' Court'),
            (9007, 'Belfast and Newtownabbey Magistrates'' Court',
                   'Belfast & Newtownabbey Mags'' Court')
        ) AS changes(id, old_name, new_name)
        ORDER BY id
    LOOP
        SELECT name INTO v_name
        FROM local_justice_areas_legacy
        WHERE local_justice_area_id = r.id
        FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION
                'PO-11411: local_justice_areas_legacy ID % is missing', r.id;
        END IF;

        IF v_name IS DISTINCT FROM r.old_name
           AND v_name IS DISTINCT FROM r.new_name THEN
            RAISE EXCEPTION
                'PO-11411: local_justice_areas_legacy ID % has unexpected name: %',
                r.id, v_name;
        END IF;

        UPDATE local_justice_areas_legacy
        SET name = r.new_name
        WHERE local_justice_area_id = r.id
          AND name IS DISTINCT FROM r.new_name;
    END LOOP;
END;
$migration$;
