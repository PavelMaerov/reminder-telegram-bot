-- liquibase formatted sql

-- changeset pavel:create task
CREATE TABLE public.task
(   id integer PRIMARY KEY,
    chat_id bigint NOT NULL,
    get_time timestamp without time zone NOT NULL,
    plan_time timestamp without time zone NOT NULL,
    fact_time timestamp without time zone,
    message varchar(255) NOT NULL
)

