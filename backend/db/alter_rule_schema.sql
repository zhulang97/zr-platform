-- Update t_rule table with new fields
alter table t_rule add (
  rule_type        varchar(32),
  rule_condition   varchar(256),
  priority         number(10) default 5,
  cron_expr        varchar(64),
  last_executed_at timestamp,
  created_at       timestamp
);

-- Rebuild t_anomaly_snapshot table with more detailed fields
drop table t_anomaly_snapshot;

create table t_anomaly_snapshot (
  snapshot_id       number(19) primary key,
  rule_id          number(19),
  rule_code        varchar(64),
  rule_name        varchar(128),
  person_id        number(19),
  person_name      varchar(64),
  anomaly_type     varchar(32),
  severity         number(10),
  condition_data   varchar(512),
  matched_values   clob,
  created_at       timestamp not null
);

create index ix_snapshot_rule on t_anomaly_snapshot(rule_id);
create index ix_snapshot_person on t_anomaly_snapshot(person_id);
create index ix_snapshot_created_at on t_anomaly_snapshot(created_at);
