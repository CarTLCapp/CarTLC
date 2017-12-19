# --- Database schema

# --- !Ups

ALTER TABLE truck rename truck_v6;

CREATE TABLE truck (
    id                  int auto_increment primary key,
    truck_number        varchar(64),
    license_plate       varchar(64),
    upload_id           int,
    project_id          int default 0,
    company_name_id     int default 0,
    created_by          int default 0,
    created_by_client   bit default 0
);
# --- !Downs

DROP TABLE if exists truck;
ALTER TABLE truck_v6 rename truck;