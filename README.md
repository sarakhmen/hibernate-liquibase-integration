# Hibernate-Liquibase integration

Let's move further and get acquainted with Liquibase - a database changes management tool.
In this task you're provided with created `User` and `Role` models. 
### Your tasks are:
- configure `hibernate.cfg.xml` to properly integrate hibernate with liquibase
- add Liquibase dependency
- add Liquibase plugin
- create and configure `liquibase.properties` file in your resources folder
- add changesets for creating users, roles, user_role tables
- add changesets for inserting two users: a user with an email `admin@example.com` with role `ADMIN`, and user with email `user@example.com` with role `USER`
### Additional tips:
- pay attention that changesets should be written in the database-agnostic YAML format (files with `.yaml/.yml` extension)
- you can test your solution locally, using custom `Main` class
- to apply changesets properly you can use Liquibase API within your source code, or simply run `mvn liquibase:update` in a console. Please refer to the documentation for more details