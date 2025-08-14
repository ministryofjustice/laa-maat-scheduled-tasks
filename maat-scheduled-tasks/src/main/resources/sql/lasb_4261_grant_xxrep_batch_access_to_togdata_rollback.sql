/*
 Purpose    : Revoke the central print job execution access for the TOGDATA user.
 JIRA       : LASB-4261
 Author     : Ganga Nitta
 History    :
 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      14/08/2025   Ganga Nitta         Initial Version
*/

--ROLLBACK script
REVOKE EXECUTE ON rep.xxrep_batch TO TOGDATA;