-- DBUPDATE-032-0.SQL

create table T_USER_REQUEST
(
    USRQ_ID_C          varchar(36)  not null,
    USRQ_USERNAME_C    varchar(50)  not null,
    USRQ_PASSWORD_C    varchar(100) not null,
    USRQ_EMAIL_C       varchar(100) not null,
    USRQ_PRIVATEKEY_C  varchar(100),
    USRQ_CREATEDATE_D  datetime     not null,
    USRQ_STATUS_C      varchar(10)  not null,
    USRQ_PROCESSDATE_D datetime,
    USRQ_PROCESSEDBY_C varchar(36),
    USRQ_IDLOCALE_C    varchar(10) default 'en', --default is English like in original user creation
    primary key (USRQ_ID_C)
);

--indexes
create index IDX_USRQ_USERNAME_C on T_USER_REQUEST (USRQ_USERNAME_C);
create index IDX_USRQ_STATUS_C on T_USER_REQUEST (USRQ_STATUS_C);

--foreign keys
alter table T_USER_REQUEST
    add constraint FK_USRQ_PROCESSEDBY_C
        foreign key (USRQ_PROCESSEDBY_C) references T_USER (USE_ID_C);
alter table T_USER_REQUEST
    add constraint FK_USRQ_IDLOCALE_C
        foreign key (USRQ_IDLOCALE_C) references T_LOCALE (LOC_ID_C);

--version
update T_CONFIG
set CFG_VALUE_C = '32'
where CFG_ID_C = 'DB_VERSION';