# --- Database schema

# --- !Ups

create table version (
  id                int auto_increment primary key,
  skey              varchar(255),
  ivalue            int default 1
);

create table client (
  id                int auto_increment primary key,
  first_name        varchar(255),
  last_name         varchar(255),
  device_id         varchar(255),
  last_ping         datetime,
  disabled          bit default 0,
  reset_upload      bit default 0
);

create table company (
  id                int auto_increment primary key,
  name              varchar(255),
  street            varchar(255),
  city              varchar(128),
  state             varchar(64),
  zipcode           varchar(64),
  created_by        int default 0,
  disabled          bit default 0
);

alter table company add constraint c_c_tech_id foreign key (created_by) references client (id) on delete restrict on update restrict;

create table project (
  id                int auto_increment primary key,
  name              varchar(64),
  disabled          bit default 0
);

create table equipment (
  id                int auto_increment primary key,
  name              varchar(128),
  created_by        int default 0,
  disabled          bit default 0
);

alter table equipment add constraint c_eq_tech_id foreign key (created_by) references client (id) on delete restrict on update restrict;

create table project_equipment_collection (
  id                int auto_increment primary key,
  project_id        int,
  equipment_id      int
);

alter table project_equipment_collection add constraint c_pec_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table project_equipment_collection add constraint c_pec_equipment_id foreign key (equipment_id) references equipment (id) on delete restrict on update restrict;

create table note (
  id                int auto_increment primary key,
  name              varchar(128),
  type              smallint,
  created_by        int default 0,
  disabled          bit default 0
);

alter table note add constraint c_n_tech_id foreign key (created_by) references client (id) on delete restrict on update restrict;

create table project_note_collection (
  id                int auto_increment primary key,
  project_id        int,
  note_id           int
);

alter table project_note_collection add constraint c_pnc_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table project_note_collection add constraint c_pnc_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table picture_collection (
  id                int auto_increment primary key,
  collection_id     int,
  picture           varchar(255)
);

create table entry_equipment_collection (
  id                int auto_increment primary key,
  collection_id     int,
  equipment_id      int
);

alter table entry_equipment_collection add constraint c_eec_equipment_id foreign key (equipment_id) references equipment (id) on delete restrict on update restrict;

create table entry_note_collection (
  id                int auto_increment primary key,
  collection_id     int,
  note_id           int,
  note_value        varchar(255)
);

alter table entry_note_collection add constraint c_enc_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table entry (
  id                       int auto_increment primary key,
  tech_id                  int,
  entry_time               datetime,
  project_id               int,
  address_id               int,
  equipment_collection_id  int,
  note_collection_id       int,
  picture_collection_id    int,
  truck_number             int
);

alter table entry add constraint c_e_entry_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table entry add constraint c_e_entry_address_id foreign key (address_id) references company (id) on delete restrict on update restrict;
alter table entry add constraint c_e_tech_id foreign key (tech_id) references client (id) on delete restrict on update restrict;

# --- !Downs

drop table if exists version;
drop table if exists entry;
drop table if exists project_equipment_collection;
drop table if exists project_note_collection;
drop table if exists picture_collection;
drop table if exists entry_equipment_collection;
drop table if exists entry_note_collection;
drop table if exists client;
drop table if exists equipment;
drop table if exists note;
drop table if exists company;
drop table if exists project;
