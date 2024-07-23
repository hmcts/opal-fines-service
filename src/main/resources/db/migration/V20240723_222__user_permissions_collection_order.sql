/**
*
* OPAL Program
*
* MODULE      : collection_orders.sql
*
* DESCRIPTION : Create user permissions for collection order
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change 
* ----------    --------     --------    ---------------------------------------------------------------------------------------------------------
* 23/07/2024    I Readman    1.0         PO-492 Create user permissions for collection order in Opal
*
**/      
INSERT INTO application_functions VALUES (500,'Collection Order');
INSERT INTO templates VALUES (500000004,'Collection Order - Authorised');
INSERT INTO template_mappings VALUES (500000004, 500);

-- Select all applicable Business Unit User Ids and insert into user_entitlements table
DO $$
DECLARE
 user_ent_id integer := 500000;
 bus_units record;
BEGIN 
FOR bus_units IN
  SELECT buu.business_unit_user_id AS bu_user_id
    FROM users u 
    JOIN business_unit_users buu ON u.user_id = buu.user_id
   WHERE u.user_id <> '500000000'
LOOP
 INSERT into user_entitlements VALUES (user_ent_id, bus_units.bu_user_id, 500);
 user_ent_id = user_ent_id + 1;
END LOOP;
END $$;