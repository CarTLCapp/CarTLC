# --- Database schema

# --- !Ups

create table message (
  id            int auto_increment primary key,
  tech_id       int,
  log_time      datetime,
  code          smallint,
  message       text,
  trace         text
);

alter table client rename technician;

create table client (
    id          int auto_increment primary key,
    name        varchar(128),
    password    varchar(128),
    is_admin    bit default 0,
    disabled    bit default 0
);

create table client_project_association (
    id             int auto_increment primary key,
    client_id      int,
    project_id     int
);

alter table client_project_association add constraint c_cpa_user_info_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_project_association add constraint c_cpa_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table picture_collection add note varchar(1028);

create table work_order (
    id             int auto_increment primary key,
    upload_id      int,
    client_id      int,
    project_id     int,
    company_id     int,
    truck_id       int
);

create table truck (
    id             int auto_increment primary key,
    truck_number   int,
    license_plate  varchar(64),
    upload_id      int
);

alter table entry rename entry_v2;

create table entry (
  id                       int auto_increment primary key,
  tech_id                  int,
  entry_time               datetime,
  project_id               int,
  company_id               int,
  equipment_collection_id  int,
  note_collection_id       int,
  picture_collection_id    int,
  truck_id                 int
);

alter table entry add constraint c2_e_entry_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table entry add constraint c2_e_entry_address_id foreign key (company_id) references company (id) on delete restrict on update restrict;
alter table entry add constraint c2_e_tech_id foreign key (tech_id) references technician (id) on delete restrict on update restrict;

alter table company add created_by_client bit default 0;
alter table company add upload_id int default 0;
alter table equipment add created_by_client bit default 0;
alter table note add created_by_client bit default 0;
alter table note add num_digits smallint default 0;
# --- !Downs

drop table if exists message;
drop table if exists client_project_association;
drop table if exists client;
alter table technician rename client;
alter table picture_collection drop column note;
drop table if exists work_order;
drop table if exists truck;
drop table if exists entry;
alter table entry_v2 rename entry;
alter table company drop column created_by_client;
alter table company drop column upload_id;
alter table equipment drop column created_by_client;
alter table note drop column created_by_client;
alter table note drop column num_digits;
