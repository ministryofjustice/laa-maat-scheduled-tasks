/*
 Purpose    : Rollback the creation of FINAL_DEFENCE_COSTS table that is used for storing the FDC data from Billing team
 JIRA       : LASB-4597

 Author     : Clement Ibuanokpe

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      28/11/2025   C Ibuanokpe      Initial Version


To be run as HUB@MAATDB

*/
DROP TABLE HUB.FINAL_DEFENCE_COSTS;