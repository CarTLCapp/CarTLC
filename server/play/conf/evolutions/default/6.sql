# --- Database schema

# --- !Ups

ALTER TABLE truck add project_id int default 0;
ALTER TABLE truck add company_name_id int default 0;

# --- !Downs

ALTER TABLE truck DROP COLUMN project_id;
ALTER TABLE truck DROP COLUMN company_name_id;

