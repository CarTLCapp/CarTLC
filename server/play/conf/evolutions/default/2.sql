# --- Database schema

# --- !Ups

create table root_project(
    id                int auto_increment primary key,
    name              varchar(64),
    disabled          bit default 0
);

alter table project add root_project_id int;
alter table project add constraint c_p_root_project_id foreign key (root_project_id) references root_project (id) on delete restrict on update restrict;
alter table technician add code int default 0;

rename table client_company_name_association to client_association;
alter table client_association add show_pictures bit default 0;
alter table client_association add show_trucks bit default 0;
alter table client_association add show_all_notes bit default 0;
alter table client_association add show_all_equipments bit default 0;

create table client_note_association (
    id              int auto_increment primary key,
    client_id       int,
    note_id         int
);

alter table client_note_association add constraint c_cna_client_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_note_association add constraint c_cna_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table client_equipment_association (
    id              int auto_increment primary key,
    client_id       int,
    equipment_id    int
);

alter table client_equipment_association add constraint c_cea_client_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_equipment_association add constraint c_cea_note_id foreign key (equipment_id) references equipment (id) on delete restrict on update restrict;

# --- !Downs

alter table project drop foreign key c_p_root_project_id;
alter table project drop root_project_id;
alter table technician drop code;
drop table if exists root_project;
alter table client_association drop show_pictures;
alter table client_association drop show_trucks;
alter table client_association drop show_all_notes;
alter table client_association drop show_all_equipments;
rename table client_association to client_company_name_association;
drop table if exists client_note_association;
drop table if exists client_equipment_association;
