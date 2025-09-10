/*
 Purpose    : Rollback the FDC_READY table that is used for storing the FDC Ready data from Billing team
 JIRA       : LASB-4401

 Author     : V Velpuri

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      03/09/2025   V Velpuri           Initial Version


To be run as HUB@MAATDB

*/
DROP TABLE HUB.FDC_READY;