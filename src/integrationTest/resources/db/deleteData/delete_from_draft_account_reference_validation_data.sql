DELETE FROM prosecutors
WHERE prosecutor_code IN (32010);

DELETE FROM prosecutors_legacy
WHERE prosecutor_id IN (32010);

DELETE FROM local_justice_areas
WHERE local_justice_area_id IN (32001, 32002, 32003);
