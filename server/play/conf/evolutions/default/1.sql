# --- Database schema

# --- !Ups

create table client (
    id          int auto_increment primary key,
    name        varchar(128),
    password    varchar(128),
    is_admin    bit default 0,
    disabled    bit default 0
);

create table project (
    id                int auto_increment primary key,
    name              varchar(64),
    disabled          bit default 0
);

create table technician (
    id                int auto_increment primary key,
    first_name        varchar(255),
    last_name         varchar(255),
    device_id         varchar(255),
    last_ping         datetime,
    disabled          bit default 0,
    reset_upload      bit default 0,
    app_version       varchar(64),
    reload_code       varchar(16)
);

create table company_name (
    id             int auto_increment primary key,
    name           varchar(255),
    disabled       bit default 0
);

create table client_company_name_association (
    id              int auto_increment primary key,
    client_id       int,
    company_name_id int
);

alter table client_company_name_association add constraint c_ccna_client_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_company_name_association add constraint c_ccna_company_name_id foreign key (company_name_id) references company_name (id) on delete restrict on update restrict;

create table client_project_association (
    id             int auto_increment primary key,
    client_id      int,
    project_id     int
);

alter table client_project_association add constraint c_cpa_client_id foreign key (client_id) references client (id) on delete restrict on update restrict;
alter table client_project_association add constraint c_cpa_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;

create table company (
    id                int auto_increment primary key,
    name              varchar(255),
    street            varchar(255),
    city              varchar(128),
    state             varchar(64),
    zipcode           varchar(64),
    created_by        int default 0,
    disabled          bit default 0,
    created_by_client int default 0,
    upload_id         int default 0
);

create table equipment (
    id                int auto_increment primary key,
    name              varchar(128),
    created_by        int default 0,
    disabled          bit default 0,
    created_by_client bit default 0
);

create table entry_equipment_collection (
    id                int auto_increment primary key,
    collection_id     int,
    equipment_id      int
);

alter table entry_equipment_collection add constraint c_eec_equipment_id foreign key (equipment_id) references equipment (id) on delete restrict on update restrict;

create table note (
    id                int auto_increment primary key,
    name              varchar(128),
    type              smallint,
    created_by        int default 0,
    disabled          bit default 0,
    created_by_client bit default 0,
    num_digits        smallint default 0
);

create table project_note_collection (
    id                int auto_increment primary key,
    project_id        int,
    note_id           int
);

alter table project_note_collection add constraint c_pnc_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table project_note_collection add constraint c_pnc_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table entry_note_collection (
    id                int auto_increment primary key,
    collection_id     int,
    note_id           int,
    note_value        varchar(255)
);

alter table entry_note_collection add constraint c_enc_note_id foreign key (note_id) references note (id) on delete restrict on update restrict;

create table message (
    id            int auto_increment primary key,
    tech_id       int,
    log_time      datetime,
    code          smallint,
    message       text,
    trace         text,
    app_version   varchar(64)
);

create table picture_collection (
    id                int auto_increment primary key,
    collection_id     int,
    picture           varchar(255),
    note              varchar(1028)
);

create table project_equipment_collection (
    id                int auto_increment primary key,
    project_id        int,
    equipment_id      int
);

alter table project_equipment_collection add constraint c_pec_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table project_equipment_collection add constraint c_pec_equipment_id foreign key (equipment_id) references equipment (id) on delete restrict on update restrict;

create table entry (
  id                       int auto_increment primary key,
  tech_id                  int,
  entry_time               datetime,
  project_id               int,
  company_id               int,
  equipment_collection_id  int,
  note_collection_id       int,
  picture_collection_id    int,
  truck_id                 int,
  status                   smallint,
  time_zone                varchar(32)
);

alter table entry add constraint c2_e_entry_project_id foreign key (project_id) references project (id) on delete restrict on update restrict;
alter table entry add constraint c2_e_entry_company_id foreign key (company_id) references company (id) on delete restrict on update restrict;
alter table entry add constraint c2_e_tech_id foreign key (tech_id) references technician (id) on delete restrict on update restrict;

# -- Thoughts:
# -- alter table entry add constraint c2_p_picture_collection_id foreign key (picture_collection_id) references picture_collection (collection_id) on delete restrict on update restrict;
# -- alter table entry add constraint c2_n_note_collection_id foreign key (note_collection_id) references note_collection (collection_id) on delete restrict on update restrict;
# -- alter table entry add constraint c2_e_equipment_collection_id foreign key ()

create table secondary_technician (
    id                  int auto_increment primary key,
    entry_id            int default 0,
    secondary_tech_id   int default 0
);

alter table secondary_technician add constraint c2_entry_id foreign key (entry_id) references entry (id) on delete restrict on update restrict;

create table strings (
    id                  int auto_increment primary key,
    string_value        varchar(255)
);

create table truck (
    id                  int auto_increment primary key,
    truck_number        varchar(64),
    license_plate       varchar(64),
    upload_id           int,
    project_id          int default 0,
    company_name_id     int default 0,
    created_by          int default 0,
    created_by_client   bit default 0
);

alter table truck add constraint c2_project_id foreign key(project_id) references project(id) on delete restrict on update restrict;
alter table truck add constraint c2_company_name_id foreign key(company_name_id) references company_name(id) on delete restrict on update restrict;

create table vehicle (
    id                      int auto_increment primary key,
    tech_id                 int,
    entry_time              datetime,
    time_zone               varchar(32),
    inspecting              int default 0,
    type_of_inspection      int default 0,
    mileage                 int default 0,
    head_lights             text,
    tail_lights             text,
    exterior_light_issues   varchar(255),
    fluid_checks            text,
    fluid_problems_detected varchar(255),
    tire_inspection         text,
    exterior_damage         varchar(255),
    other                   varchar(255),
    uploaded                bit default 0
);

alter table vehicle add constraint c2_tech_id foreign key(tech_id) references technician(id) on delete restrict on update restrict;

create table vehicle_name (
    id          int auto_increment primary key,
    name        varchar(255),
    number      smallint
);

create table version (
  id                int auto_increment primary key,
  skey              varchar(255),
  ivalue            int default 1
);

create table work_order (
    id             int auto_increment primary key,
    upload_id      int,
    client_id      int,
    project_id     int,
    company_id     int,
    truck_id       int
);

alter table work_order add constraint c2_w_project_id foreign key(project_id) references project(id) on delete restrict on update restrict;
alter table work_order add constraint c2_w_truck_id foreign key(truck_id) references truck(id) on delete restrict on update restrict;
alter table work_order add constraint c2_w_company_id foreign key(company_id) references company(id) on delete restrict on update restrict;
alter table work_order add constraint c2_w_client_id foreign key(client_id) references client(id) on delete restrict on update restrict;

# --- !Downs

drop table if exists vehicle;
drop table if exists vehicle_name;
drop table if exists work_order;
drop table if exists version;
drop table if exists strings;
drop table if exists secondary_technician;
drop table if exists entry;
drop table if exists entry_v2;
drop table if exists client_project_association;
drop table if exists client_company_name_association;
drop table if exists entry_equipment_collection;
drop table if exists project_note_collection;
drop table if exists project_equipment_collection;
drop table if exists entry_note_collection;
drop table if exists picture_collection;
drop table if exists work_order;
drop table if exists message;
drop table if exists company;
drop table if exists note;
drop table if exists truck;
drop table if exists truck_v6;
drop table if exists company_name;
drop table if exists technician;
drop table if exists client;
drop table if exists equipment;
drop table if exists project;
