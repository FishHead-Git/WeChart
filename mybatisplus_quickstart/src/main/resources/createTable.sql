create table `tb_user` (
  id int not null auto_increment primary key comment '主键',
  username varchar(50) not null comment '用户名',
  password varchar(255) not null comment '密码'
)engine=InnoDB default charset=utf8mb4;