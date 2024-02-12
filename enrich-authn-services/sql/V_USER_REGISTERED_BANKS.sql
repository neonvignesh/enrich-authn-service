CREATE TABLE users.V_USER_REGISTERED_BANKS (
    ID bigserial NOT NULL ,
    USER_ID INT NOT NULL,
    USER_NAME VARCHAR(255) NOT NULL,   
	REGISTERED_BANKS VARCHAR(5000) NOT NULL,
	CREATED_DATE_TIME TIMESTAMP NOT NULL,
    CREATION_ID VARCHAR(50) NOT NULL,
    LAST_UPDATED_DATE_TIME  TIMESTAMP,
    LAST_UPDATED_ID VARCHAR(50),
    PRIMARY KEY (ID)
);

INSERT INTO users.V_USER_REGISTERED_BANKS 
(USER_ID, USER_NAME, REGISTERED_BANKS, CREATED_DATE_TIME, CREATION_ID) 
VALUES (123,'Chandran Rajesh',
'[{"bankId": 1234,"displayName": "ICICI BANK LTD XX 2335","upiId": "231@okicici.com"},{"bankId": 1235,"displayName": "HDFC BANK LTD XX 2335","upiId": "3456@okhdfc.com"}]',
current_timestamp, 'rajan');
commit;

select * from users.V_USER_REGISTERED_BANKS;