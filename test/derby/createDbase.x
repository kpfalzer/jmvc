#!/bin/csh -f

rm -rf MyDbTest
exec java -jar ./derbyrun.jar ij <<EOF
connect 'jdbc:derby://localhost:3308/MYDBTEST;create=true';
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(
    'derby.connection.requireAuthentication',
    'true');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(
    'derby.user.MYDBTESTUSER', 'MYDBTESTPASSWORD');
CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(
    'derby.database.fullAccessUsers', 'MYDBTESTUSER');
create table MYDBTEST.STUDENT (
   ID INT NOT NULL GENERATED ALWAYS AS IDENTITY,
   AGE INT NOT NULL,
   FIRST_NAME VARCHAR(255),
   LAST_NAME VARCHAR(255),
   PRIMARY KEY (ID)
);
grant ALL PRIVILEGES ON MYDBTEST.STUDENT TO MYDBTESTUSER ;
exit;
EOF
