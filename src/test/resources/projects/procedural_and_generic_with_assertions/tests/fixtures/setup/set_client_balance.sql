UPDATE CLIENTS SET BALANCE=200 WHERE ID=66778899;
INSERT INTO TRANSACTIONS(ID_CLIENT, VALUE, TRANSACTION_TIME) VALUES (66778899, 500, CURRENT_TIMESTAMP - 1/24/6);
INSERT INTO TRANSACTIONS(ID_CLIENT, VALUE, TRANSACTION_TIME) VALUES (66778899, -100, CURRENT_TIMESTAMP - 1/24/7);
INSERT INTO TRANSACTIONS(ID_CLIENT, VALUE, TRANSACTION_TIME) VALUES (66778899, -100, CURRENT_TIMESTAMP - 1/24/8);
INSERT INTO TRANSACTIONS(ID_CLIENT, VALUE, TRANSACTION_TIME) VALUES (66778899, -100, CURRENT_TIMESTAMP - 1/24/10);