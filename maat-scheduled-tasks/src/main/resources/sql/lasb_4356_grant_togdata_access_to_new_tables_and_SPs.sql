-- run as admin to grant TOGDATA user privileges for all HUB tables and Stored Procedures
BEGIN
  -- Loop through every table in HUB
  FOR t IN (
    SELECT table_name
      FROM all_tables
     WHERE owner = 'HUB'
  ) LOOP
    EXECUTE IMMEDIATE
      'GRANT SELECT, INSERT, UPDATE, DELETE ON HUB.'
      || t.table_name
      || ' TO TOGDATA';
  END LOOP;

  -- Loop through every procedure/function/package in HUB
  FOR p IN (
    SELECT object_name
      FROM all_objects
     WHERE owner = 'HUB'
       AND object_type IN ('PROCEDURE','FUNCTION','PACKAGE')
  ) LOOP
    EXECUTE IMMEDIATE
      'GRANT EXECUTE ON HUB.'
      || p.object_name
      || ' TO TOGDATA';
  END LOOP;
END;