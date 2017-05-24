# --- Sample dataset

# --- !Ups

insert into projects (id,name) values (  1,'Five Cubits');
insert into projects (id,name) values (  2,'Digital Fleet');
insert into projects (id,name) values (  3,'Smart Witness');
insert into projects (id,name) values (  4,'Fed Ex');
insert into projects (id,name) values (  5,'Verifi');
insert into projects (id,name) values (  6,'Other');
insert into companies (id,name,street,city,state) values (  1,'IMI','1201 Mason Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  2,'Alamo RM','1155 Battery Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  3,'Ozinga','865 Market Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  4,'CW Roberts','120 4th Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  5,'CW Roberts','2222 Fillmore Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  6,'CW Roberts','499 Bay Street','San Francisco','California');
insert into companies (id,name,street,city,state) values (  7,'Point RM','1111 S Figueroa Street','Los Angeles','California');
insert into companies (id,name,street,city,state) values (  8,'Central Concrete','111 S Grand Avenue','Los Angeles','California');
insert into companies (id,name,street,city,state) values (  9,'IMI','1 S State Street','Chicago','Illinois');
insert into companies (id,name,street,city,state) values ( 10,'Alamo RM','111 N State Street','Chicago','Illinois');
insert into companies (id,name,street,city,state) values ( 11,'Ozinga','200 E Ohio Street','Chicago','Illinois');
insert into companies (id,name,street,city,state) values ( 12,'Ozinga','521 N State Street','Chicago','Illinois');
insert into companies (id,name,street,city,state) values ( 13,'CW Roberts','601 W Dayton Street','Madison','Wisconsin');
insert into companies (id,name,street,city,state) values ( 14,'Point RM','62424 University Avenue','Madison','Wisconsin');
insert into companies (id,name,street,city,state) values ( 15,'Central Concrete','2115 King Street','Madison','Wisconsin');
insert into companies (id,name,street,city,state) values ( 16,'IMI','1001 Howard Street','Detroit','Michigan');
insert into companies (id,name,street,city,state) values ( 17,'Alamo RM','1425 W Lafayette Boulevard','Detroit','Michigan');
insert into companies (id,name,street,city,state) values ( 18,'Alamo RM','100 Renaissance Center','Detroit','Michigan');
insert into companies (id,name,street,city,state) values ( 19,'Alamo RM','2155 Gratiot Avenue','Detroit','Michigan');
insert into equipments (id,name) values (  1,'OBC');
insert into equipments (id,name) values (  2,'RDT');
insert into equipments (id,name) values (  3,'VMX');
insert into equipments (id,name) values (  4,'6 pin Canbus');
insert into equipments (id,name) values (  5,'9 pin Canbus');
insert into equipments (id,name) values (  6,'Green Canbus');
insert into equipments (id,name) values (  7,'External Antenna');
insert into equipments (id,name) values (  8,'Internal Antenna');
insert into equipments (id,name) values (  9,'Repair Work');
insert into equipments (id,name) values ( 10,'Uninstall');
insert into equipments (id,name) values ( 11,'Other');
insert into equipments (id,name) values ( 12,'Tablet');
insert into equipments (id,name) values ( 13,'Canbus');
insert into equipments (id,name) values ( 14,'KP1S');
insert into equipments (id,name) values ( 15,'CPI');
insert into equipments (id,name) values ( 16,'SVC 1080');
insert into equipments (id,name) values ( 17,'Modem');
insert into equipments (id,name) values ( 18,'Driver Facing Camera');
insert into equipments (id,name) values ( 19,'Back Up Camera');
insert into equipments (id,name) values ( 20,'Side Camera 1');
insert into equipments (id,name) values ( 21,'Side Camera 2');
insert into equipments (id,name) values ( 22,'DVR');
insert into equipments (id,name) values ( 23,'Mobileye');
insert into equipments (id,name) values ( 24,'Backup Sensors');
insert into equipments (id,name) values ( 25,'Re-Calibrate');
insert into equipments (id,name) values ( 26,'V3 Full Install');
insert into equipments (id,name) values ( 27,'V4 Full Install');
insert into equipments (id,name) values ( 29,'Admix Tank');
insert into equipments (id,name) values ( 30,'Nozzle');
insert into equipments (id,name) values ( 31,'FDM Upgrade');
insert into equipments (id,name) values ( 32,'Crossover Upgrade');
insert into equipments (id,name) values ( 33,'WTAA Install');
insert into equipments (id,name) values ( 34,'Commissioning');

# --- !Downs

delete from projects;
delete from companies;
delete from equipments;
