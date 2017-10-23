-- NB onbekende versie
    create table database_inout (
        id number(19,0) not null,
        db_alias varchar2(255 char),
        col_x varchar2(255 char),
        col_y varchar2(255 char),
        database_name varchar2(255 char),
        host_name varchar2(255 char),
        name varchar2(255 char),
        organization_id number(10,0),
        password varchar2(255 char),
        port number(10,0),
        db_schema varchar2(255 char),
        srs varchar2(255 char),
        database_type varchar2(255 char) not null,
        inout_type varchar2(255 char) not null,
        url varchar2(255 char),
        user_id number(10,0),
        username varchar2(255 char),
        webservice_db number(1,0) not null,
        primary key (id)
    );

    create table input_output (
        id number(19,0) not null,
        input_output_datatype varchar2(255 char) not null,
        file_name varchar2(255 char),
        name varchar2(255 char),
        organization_id number(10,0),
        srs varchar2(255 char),
        table_name varchar2(255 char),
        template_output varchar2(255 char),
        input_output_type varchar2(255 char) not null,
        user_id number(10,0),
        database_id number(19,0),
        primary key (id)
    );

    create table mail (
        id number(19,0) not null,
        from_email_address varchar2(255 char),
        smtp_host varchar2(255 char) not null,
        subject varchar2(255 char),
        to_email_address varchar2(255 char) not null,
        primary key (id)
    );

    create table organization (
        id number(10,0) not null,
        name varchar2(255 char) not null,
        upload_path varchar2(255 char) not null,
        primary key (id)
    );

    create table organization_users (
        organization_id number(10,0),
        users_id number(10,0) not null,
        primary key (users_id),
        unique (users_id)
    );

    create table output_organization (
        organization_id number(10,0) not null,
        output_id number(19,0) not null
    );

    create table post_action (
        id number(10,0) not null,
        class_name varchar2(255 char) not null,
        label varchar2(255 char) not null,
        primary key (id)
    );

    create table post_action_param (
        id number(10,0) not null,
        param varchar2(255 char) not null,
        value varchar2(255 char) not null,
        primary key (id)
    );

    create table process (
        id number(19,0) not null,
        actions clob not null,
        append_table number(1,0) not null,
        drop_table number(1,0) not null,
        features_end number(10,0),
        features_start number(10,0),
        name varchar2(255 char),
        organization_id number(10,0),
        remarks varchar2(255 char),
        user_id number(10,0),
        user_name varchar2(255 char),
        writer_type varchar2(255 char) not null,
        input_id number(19,0) not null,
        mail_id number(19,0) not null,
        output_id number(19,0) not null,
        process_status_id number(19,0) not null,
        schedule number(19,0),
        primary key (id)
    );

    create table process_status (
        id number(19,0) not null,
        executing_job_uuid varchar2(255 char),
        message clob,
        process_status_type varchar2(255 char) not null,
        primary key (id)
    );

    create table schedule (
        id number(19,0) not null,
        cron_expression varchar2(120 char) not null,
        from_date date,
        job_name varchar2(120 char) not null,
        schedule_type varchar2(255 char) not null,
        primary key (id)
    );

    create table users (
        id number(10,0) not null,
        is_admin number(1,0) not null,
        name varchar2(255 char) not null,
        password varchar2(255 char) not null,
        primary key (id)
    );

    alter table input_output 
        add constraint FK3D134716341B5076 
        foreign key (database_id) 
        references database_inout;

    alter table organization_users 
        add constraint FKDABFEBFCA0C96776 
        foreign key (organization_id) 
        references organization;

    alter table organization_users 
        add constraint FKDABFEBFC8BF8659E 
        foreign key (users_id) 
        references users;

    alter table output_organization 
        add constraint FK7301A7B1A0C96776 
        foreign key (organization_id) 
        references organization;

    alter table output_organization 
        add constraint FK7301A7B11CCF9206 
        foreign key (output_id) 
        references input_output;

    alter table process 
        add constraint FKED8D1E6F4594B9CA 
        foreign key (schedule) 
        references schedule;

    alter table process 
        add constraint FKED8D1E6F1CCF9206 
        foreign key (output_id) 
        references input_output;

    alter table process 
        add constraint FKED8D1E6F5B1356DD 
        foreign key (process_status_id) 
        references process_status;

    alter table process 
        add constraint FKED8D1E6F80DCC4F6 
        foreign key (mail_id) 
        references mail;

    alter table process 
        add constraint FKED8D1E6FB72FF29D 
        foreign key (input_id) 
        references input_output;

    create sequence hibernate_sequence;
