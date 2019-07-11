# --- Database schema

# --- !Ups

create table flow_element (
    id                int auto_increment primary key,
    line_num          smallint default 0,
    prompt            text,
    prompt_type       tinyint default 0,
    request_image     tinyint default 0
);

create table flow (
    id                int auto_increment primary key,
    sub_project_id    int
);

alter table flow add constraint c_f_sub_project_id foreign key (sub_project_id) references project (id) on delete restrict on update restrict;

create table flow_note_collection (
    id                int auto_increment primary key,
    flow_element_id   int,
    note_id           int
);

alter table flow_note_collection add constraint c_fnc_flow_element_id foreign key (flow_element_id) references flow_element (id) on delete restrict on update restrict;
alter table flow_note_collection add constraint c_fnc_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table flow_element_collection (
    id                int auto_increment primary key,
    flow_id           int,
    flow_element_id   int
);

alter table flow_element_collection add constraint c_fec_flow_id foreign key (flow_id) references flow (id) on delete restrict on update restrict;
alter table flow_element_collection add constraint c_fec_flow_element_id foreign key (flow_element_id) references flow_element (id) on delete restrict on update restrict;

alter table picture_collection add flow_element_id int default 0;

# --- !Downs

alter table picture_collection drop flow_element_id;
alter table flow_element_collection drop foreign key c_fec_flow_id;
alter table flow_element_collection drop foreign key c_fec_flow_element_id;
drop table if exists flow_element_collection;
alter table flow_note_collection drop foreign key c_fnc_flow_element_id;
alter table flow_note_collection drop foreign key c_fnc_note_id;
drop table if exists flow_note_collection;
alter table flow drop foreign key c_f_sub_project_id;
drop table if exists flow;
drop table if exists flow_element;
