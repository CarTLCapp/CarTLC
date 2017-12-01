# --- Database schema

# --- !Ups

ALTER TABLE truck add project_id int default 0;
ALTER TABLE truck add company_id int default 0;

# --- !Downs

ALTER TABLE truck DROP COLUMN project_id;
ALTER TABLE truck DROP COLUMN company_id;

