-- AI Integration Tables for Vector Search and Anomaly Cases

-- Person Vector Table (for semantic search)
create table t_person_vector (
  person_id      number(19) primary key,
  vector         clob,
  updated_at     timestamp not null,
  constraint fk_person_vector_person foreign key (person_id) references t_person(person_id)
);

-- Anomaly Case Table
create table t_anomaly_case (
  case_id        number(19) primary key,
  person_id      number(19) not null,
  title          varchar(256) not null,
  description    clob,
  anomaly_type  varchar(64),
  severity       number(10),
  resolution     clob,
  handler_user_id number(19),
  resolved_at    timestamp,
  created_at     timestamp not null,
  constraint fk_anomaly_case_person foreign key (person_id) references t_person(person_id),
  constraint fk_anomaly_case_handler foreign key (handler_user_id) references t_user(user_id)
);

-- Anomaly Case Vector Table (for semantic search)
create table t_anomaly_case_vector (
  case_id        number(19) primary key,
  vector         clob,
  updated_at     timestamp not null,
  constraint fk_anomaly_case_vector_case foreign key (case_id) references t_anomaly_case(case_id)
);

-- Indexes
create index ix_anomaly_case_person on t_anomaly_case(person_id);
create index ix_anomaly_case_type on t_anomaly_case(anomaly_type);
create index ix_anomaly_case_severity on t_anomaly_case(severity);
create index ix_anomaly_case_created_at on t_anomaly_case(created_at);
create index ix_anomaly_case_resolved_at on t_anomaly_case(resolved_at);

-- User-Defined Functions for Cosine Similarity (H2 Compatible)
-- Note: These are simplified versions. For production with Milvus, use native Milvus search.
