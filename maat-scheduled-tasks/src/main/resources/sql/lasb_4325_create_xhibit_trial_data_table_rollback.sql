/*
 Purpose    : Rollback the XHIBIT_TRIAL_DATA table that is used to store data pulled in from XHIBIT
 JIRA       : LASB-4325
 Author     : L Murphy
 History    :
 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      06/08/2025   L Murphy         Initial Version
To be run as HUB@MAATDB
*/
-- Destroys the table, its PK index, and any grants
DROP TABLE hub.xhibit_trial_data
    CASCADE CONSTRAINTS   -- drops FK references if any exist
  PURGE;                -- skips the Recycle Bin (optional)