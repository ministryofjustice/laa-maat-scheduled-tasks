/*
 Purpose    : Rollback the BILLING_DATA_FEED_LOG table that is used for resending the CCLF/CCR extract on demand
 JIRA       : LASB-4363

 Author     : J Hunt

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      13/08/2025   J Hunt           Initial Version


To be run as TOGDATA@MAATDB

*/
DROP TABLE BILLING_DATA_FEED_LOG;