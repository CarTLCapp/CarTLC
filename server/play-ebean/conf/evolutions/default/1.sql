# --- First database schema

# --- !Ups

create table client (
  id                int auto_increment primary key,
  first_name        varchar(255),
  last_name         varchar(255),
  disabled          bit
);

create table company (
  id                int auto_increment primary key,
  name              varchar(255),
  street            varchar(255),
  city              varchar(128),
  state             varchar(64),
  disabled          bit
);

create table projects (
  id                int auto_increment primary key,
  name              varchar(64),
  disabled          bit
);

create table equipments (
  id                int auto_increment primary key,
  name              varchar(128),
  disabled          bit
);

create table collection_project_equipment (
  id                int auto_increment primary key,
  project_id        int,
  equipment_id      int
);

alter table collection_project_equipment add constraint fk_cpe_project_id foreign key (project_id) references projects (id) on delete restrict on update restrict;
alter table collection_project_equipment add constraint fk_cpe_equipment_id foreign key (equipment_id) references equipments (id) on delete restrict on update restrict;

create table notes (
  id                int auto_increment primary key,
  name              varchar(128),
  type              smallint,
  disabled          bit
);

create table collection_project_note (
  id                int auto_increment primary key,
  project_id        int,
  note_id           int
);

alter table collection_project_note add constraint fk_cpn_project_id foreign key (project_id) references projects (id) on delete restrict on update restrict;
alter table collection_project_note add constraint fk_cpn_note_id foreign key (note_id) references notes (id) on delete restrict on update restrict;

create table pictures (
  id                int auto_increment primary key,
  filename          varchar(255)
);

create table picture_collection (
  id                int auto_increment primary key,
  collection_id     int,
  picture_id        int
);

alter table picture_collection add constraint fk_picture_id foreign key (picture_id) references pictures (id) on delete restrict on update restrict;

create table entries (
  id                       int auto_increment primary key,
  entry_time               date,
  project_id               int,
  address_id               int,
  equipment_id             int,
  picture_collection_id    int,
  truck_number             int
);

create table states (
  id        int auto_increment primary key,
  name      varchar(64),
  abbr      varchar(8)
);

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists users;
drop table if exists companies;
drop table if exists projects;
drop table if exists equipments;
drop table if exists collection_project_equipment;
drop table if exists notes;
drop table if exists collection_project_note;
drop table if exists pictures;
drop table if exists picture_collection;
drop table if exists entries;

SET REFERENTIAL_INTEGRITY TRUE;

