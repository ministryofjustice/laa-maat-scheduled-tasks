/*
 Purpose    : Rollback the FDC_READY table that is used for storing the FDC Ready data from Billing team
 JIRA       : LASB-4598

 Author     : C Ibuanokpe

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      08/12/2025   C Ibuanokpe      Initial Version


To be run as HUB@MAATDB

*/
DROP TABLE HUB.FDC_READY;