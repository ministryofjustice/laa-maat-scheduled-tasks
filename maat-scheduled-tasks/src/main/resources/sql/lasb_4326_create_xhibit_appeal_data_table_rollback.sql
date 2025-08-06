/*
 Purpose    : Rollback the XHIBIT_APPEAL_DATA table that is used to store data pulled in from XHIBIT
 JIRA       : LASB-4326

 Author     : J Hunt

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      05/08/2025   J Hunt           Initial Version


To be run as HUB@MAATDB

*/
-- Destroys the table, its PK index, and any grants
DROP TABLE hub.xhibit_appeal_data
    CASCADE CONSTRAINTS   -- drops FK references if any exist
  PURGE;                -- skips the Recycle Bin (optional)