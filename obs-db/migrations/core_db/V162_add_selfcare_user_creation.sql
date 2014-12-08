Insert into m_role values(0,'selfcare','selfcare only');
Create table del_temp (permission_name varchar(50));
Insert into del_temp values ('CHANGEPLAN_ORDER');
Insert into del_temp values ('CREATE_ALLOCATION');
Insert into del_temp values ('CREATE_CLIENT');
Insert into del_temp values ('CREATE_MEDIAASSET');
Insert into del_temp values ('CREATE_ORDER');
Insert into del_temp values ('CREATE_PAYMENT');
Insert into del_temp values ('CREATE_SELFCARE');
Insert into del_temp values ('CREATE_TICKET');
Insert into del_temp values ('EMAILVERIFICATION_SELFCARE');
Insert into del_temp values ('GENERATENEWPASSWORD_SELFCARE');
Insert into del_temp values ('ONLINE_PAYMENTGATEWAY');
Insert into del_temp values ('READ_ADDRESS');
Insert into del_temp values ('READ_ALLOCATION');
Insert into del_temp values ('READ_BILLMASTER');
Insert into del_temp values ('READ_CLIENT');
Insert into del_temp values ('READ_FINANCIALTRANSACTIONS');
Insert into del_temp values ('READ_MEDIAASSET');
Insert into del_temp values ('READ_ORDER');
Insert into del_temp values ('READ_PAYMENTGATEWAYCONFIG');
Insert into del_temp values ('READ_PRICE');
Insert into del_temp values ('READ_SELFCARE');
Insert into del_temp values ('READ_TICKET');
Insert into del_temp values ('READ_USER');
Insert into del_temp values ('REGISTER_SELFCARE');
Insert into del_temp values ('RENEWAL_ORDER');
Insert into del_temp values ('SELFREGISTRATION_ACTIVATE');
Insert into del_temp values ('UPDATE_ORDER');
Insert into del_temp values ('UPDATE_SELFCARE');
Insert into m_role_permission 
Select (Select id from m_role where name='selfcare') as rid,b.id from del_temp a,m_permission b where a.permission_name=b.code;
insert into m_appuser values (0,0,1,Null,'selfcare','selfcare only','c82109b2c879cc8106b518dd47521fedf04a2be41299feb31bbec033c1da922a',Null,0,1,1,1,1);
Insert into m_appuser_role values ((Select id from m_appuser where username='selfcare'),(Select id from m_role where name='selfcare'));
Drop table del_temp;