drop database if exists gps;
create database if not exists gps;
use gps;

drop user if exists 'userman';
create user 'userman'@'%' identified by 'random secure password';
grant all privileges on gps.* to 'userman';

drop table if exists Channel_User;
drop table if exists Message;
drop table if exists Channel;
drop table if exists User;

-- User -------------------------------------
create table if not exists User (
	id  int not null primary key auto_increment,
	username varchar(25) not null unique,
	password_hash char(64) not null
);
-- Channel ----------------------------------
create table if not exists Channel (
    id int not null primary key auto_increment,
    creator_id int not null,
    name varchar(64) not null unique,

    foreign key (creator_id) references User(id)  on delete cascade 
);
-- Channel_User ------------------------------
create table if not exists Channel_User (
	channel_id int not null,
    user_id int not null,
    
    primary key (channel_id,user_id),
	foreign key (channel_id) references Channel(id) on delete cascade,
	foreign key (user_id) references User(id) on delete cascade
);
-- Message ----------------------------------
CREATE TABLE IF NOT EXISTS Message (
    id INT NOT NULL PRIMARY KEY,
    sender_id INT NOT NULL,
    channel_id INT NOT NULL,
    moment_sent DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type ENUM('text', 'file') NOT NULL,
    content VARCHAR(512) NOT NULL,
    FOREIGN KEY (sender_id)
        REFERENCES User (id),
    FOREIGN KEY (channel_id)
        REFERENCES Channel (id) on delete cascade
);
-- Setup -----------------------------------
insert into User(id,username,password_hash) values(1,'Admin','3fw42nemv0hxxa98e8rn32syomxixtp9259v0mebmrmi91evfb');
insert into Channel(id,creator_id,name) values(1,1,'General');
-- Triggers --------------------------------
drop trigger if exists generalAddition;
delimiter $$
create trigger generalAddition after insert on user 
	for each row 
begin
	insert into channel_user(channel_id, user_id) values(1, new.id);
end $$
delimiter ;



