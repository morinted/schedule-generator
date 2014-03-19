set search_path = "Schedules";

DROP TABLE "Schedules".activities;
DROP TABLE "Schedules".sections;
DROP TABLE "Schedules".courses;

CREATE TABLE "Schedules".courses
(
  code text NOT NULL,
  description text,
  CONSTRAINT courses_pkey PRIMARY KEY (code)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Schedules".courses
  OWNER TO tmori035;

CREATE TABLE "Schedules".sections
(
  sname text NOT NULL,
  code text,
  semester integer NOT NULL,
  requireddgd integer,
  requiredtut integer,
  requiredlab integer,
  CONSTRAINT sections_pkey PRIMARY KEY (sname, semester),
  CONSTRAINT sections_code_fkey FOREIGN KEY (code)
      REFERENCES "Schedules".courses (code) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Schedules".sections
  OWNER TO tmori035;


CREATE TABLE "Schedules".activities
(
  atype text NOT NULL,
  anumber integer NOT NULL,
  sname text NOT NULL,
  semester integer NOT NULL,
  dayofweek text,
  starttime time without time zone,
  endtime time without time zone,
  place text,
  professor text,
  CONSTRAINT activities_pkey PRIMARY KEY (sname, semester, atype, anumber),
  CONSTRAINT activities_sname_fkey FOREIGN KEY (sname, semester)
      REFERENCES "Schedules".sections (sname, semester) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Schedules".activities
  OWNER TO tmori035;
