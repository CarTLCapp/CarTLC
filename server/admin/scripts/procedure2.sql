
1. Dump all data from the server

2. Edit the dump file, delete all references to play_evolutions from the dump file. 
   You do not want this table to be modified when you will eventually source this file.

3. Now empty everything in the database:

mysql -u cartlc -p
cartcl
use cartlc
source drop.sql

4. Now start up new server against this empty database

5. Source modified dump file:

mysql -u cartlc -p
cartcl
use cartlc
source dump-...

6. Now the data is in there, but unfortunately against the wrong evolution version.
   That is the database still needs some modifications or the existing data will fail against it.
   And the new tables have not been removed by the source.
   So execute this:
	DELETE FROM `play_evolutions` WHERE `play_evolutions`.`id` = 2;
	DELETE FROM `play_evolutions` WHERE `play_evolutions`.`id` = 3;
	drop table if exists root_project;
	drop table if exists client_association;
	drop table if exists client_note_association;
	drop table if exists client_equipment_association;
	drop table if exists flow;
	drop table if exists flow_element_collection;
	drop table if exists flow_element;
	drop table if exists flow_note_collection;
	drop table if exists prompt;
	drop table if exists entry_v2;
	drop table if exists truck_v6;

7. Restart server. Now the evolution second line should happen. Everything should be fine with the data.
