 --- Database schema

# --- !Ups

ALTER TABLE technician ADD email varchar(256);

CREATE TABLE strings (
    id                  int auto_increment primary key,
    string_value        varchar(255)
);

CREATE TABLE vehicles (
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
    other                   varchar(255)
    uploaded                bit default 0
);

# --- !Downs

ALTER TABLE technician DROP email;
DROP TABLE if exists strings;
DROP TABLE if exists vehicles;