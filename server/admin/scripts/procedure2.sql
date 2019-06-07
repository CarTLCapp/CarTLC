
1. Dump all data from the server

2. Edit the dump file, delete play_evolutions line from the dump file. You don't want this table to modified
   when you will eventually source this file.

3. Now empty everything in the database

mysql -u cartlc -p
cartcl
use cartlc
source drop.sql

4. Now start up new server against this empty database

5. Source modified dump file. 

mysql -u cartlc -p
cartcl
use cartlc
source dump-...

6. Now the data is in there, but unfortunately against the wrong evolution version.
   So execute this:
	DELETE FROM `play_evolutions` WHERE `play_evolutions`.`id` = 2;
	alter table client_association drop show_pictures;
	alter table client_association drop show_trucks;
	alter table client_association drop show_all_notes;
	alter table client_association drop show_all_equipments;
	rename table client_association to client_company_name_association;
	drop table if exists root_project;
	drop table if exists client_note_association;
	drop table if exists client_equipment_association;
	drop table if exists entry_v2;
	drop table if exists truck_v6;

7. Restart server. Now the evolution second line should happen. Everything should be fine with the data.
