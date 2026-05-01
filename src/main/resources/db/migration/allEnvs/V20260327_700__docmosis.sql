create sequence document_templates_id_seq;
create type document_template_type_enum as enum ('HMRC_REPORT');

create table document_template
(
    id                 bigint                      not null default nextval('document_templates_id_seq'),
    document_type      document_template_type_enum not null,
    template_name      varchar(255)                not null,
    created_at         timestamp with time zone    not null default now(),
    active_from        timestamp                   NOT NULL,
    active_to          timestamp                   NULL,
    data_mapping_class varchar(255)                not null,
    primary key (id)
);