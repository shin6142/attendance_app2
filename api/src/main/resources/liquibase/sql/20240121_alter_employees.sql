DROP TABLE IF EXISTS employees;
create table employees (
                           employee_id uuid NOT NULL,
                           name varchar(20) NOT NULL,
                           primary key (employee_id)
);
