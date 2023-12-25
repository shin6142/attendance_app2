# attendance management application
## Description
This application automates recording monthly attendances for Slack and Freee User.

## Stack
- Kotlin
- Spring Boot
- TypeScript
- React
- PostgreSQL
- jooq
- liquibase
- openapi-generator
- Docker


![Architecture](/image/architecture.png)

![Authentication](/image/authentication.png)


## set up
### create application.properties
```
cd api/src/main/resources/
touch application.properties
```
application.properties needs following properties.
```
spring.datasource.url=jdbc:postgresql://db:5432/db
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.liquibase.change-log=classpath:liquibase/changelog.xml
slack.token=
freee.clientId=
freee.clientSecret=
```

### initialize application & start application
```
$ make initialize
```

### start application
```
$ make start
```

### stop application
```
$ make stop
```