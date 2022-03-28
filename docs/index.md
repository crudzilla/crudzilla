## CRUDZilla --- Powerful and Flexible CRUD's

CRUDZilla is a lib to help developers avoid repeating some code when creating simple (and complex) CRUD's for your application.
This lib is built over SpringBoot and [QueryDSL](http://querydsl.com/).

A complete sample project could be found at [CRUDZilla Demo Project](https://github.com/crudzilla/demo).

### Getting Started

Install using Maven:

```xml
<dependency>
    <groupId>io.github.crudzilla</groupId>
    <artifactId>crudzilla</artifactId>
    <version>1.0.0</version>
</dependency>
```

Also, you'll have to use QueryDSL JPA and SQL. For that, you'll need the QueryDSL plugins:

```xml
<plugin>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-maven-plugin</artifactId>
    <version>5.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>export</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <jdbcDriver>org.h2.Driver</jdbcDriver>
        <jdbcUrl>jdbc:h2:mem:CATALOGNAME;DATABASE_TO_UPPER=FALSE;INIT=RUNSCRIPT FROM 'file:src/main/resources/sql/schema.sql'</jdbcUrl>
        <packageName>io.github.crudzilla.example.sqlentities</packageName>
        <schemaToPackage>false</schemaToPackage>
        <namePrefix>S</namePrefix>
        <targetFolder>${project.basedir}/target/generated-sources/java</targetFolder>
        <sourceFolder/>
        <jdbcUser>sa</jdbcUser>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.200</version>
        </dependency>
    </dependencies>
</plugin>

<plugin>
    <groupId>com.mysema.maven</groupId>
    <artifactId>apt-maven-plugin</artifactId>
    <version>1.1.3</version>
    <executions>
        <execution>
            <goals>
                <goal>process</goal>
            </goals>
            <configuration>
                <outputDirectory>target/generated-sources/java</outputDirectory>
                <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
                <options>
                    <!--<querydsl.excludedClasses>br.log.Revisao</querydsl.excludedClasses>-->
                </options>
                <compilerOptions>
                    <source>17</source>
                </compilerOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

These plugins config above suppose you have a `schema.sql` file that represents your database schema. In this example, we'll use:

```sql
--TODO: add schema file here
```

Also, you'll need to add the annotation `@CRUDZillaGlobalConfig` at one of your Springboot config classes.
```java
@Configuration
@CRUDZillaGlobalConfig(scanPackage="io.github.crudzilla.demo")
public class AppConfig {
    ...
}
```

The properties you can set up here are:
- scanPackage: the base package name that CRUDZilla reflections should scan in order to find your Entities, Repositories and etc. Usually, the same as your SpringBoot basePackageScan.
- domainPackagePrefix (default: .domain) and appPackagePrefix (default: .app): this lib works with some conventions. It supposes your entities are inside some package that represent the domain, example: `io.github.crudzilla.demo.users.domain.User`. In this case, with the default configuration, it will expect that there's a `UserRepository` at the same package. The other classes (covered later in this tutorial) will be placed on a appPackage like `io.github.crudzilla.demo.users.app.UserQueryBuilder`

### Preparing the Entity
Now, you should prepare your JPA Entity with `@CRUDZillaConfig` 

```java
@Entity
@Table
@CRUDZillaConfig(key = "users", disableGetAll = true)
public class User implements CRUDZillaEntity<Integer>, UserDetails {
    ...
}
```

In @CRUDZillaConfig you need to provide a Key. The Key is used to generate tha url's for all the operations on your entity. All the url's below will be created for user:

- `GET /api/auth/crudzilla/users` search entities with columns, filters, paging and sort options
- `POST /api/auth/crudzilla/users` create of update entity based on a Form dto (see ahead the Form pattern)
- `POST /api/auth/crudzilla/users/{id}/toggle-active` if your entity implement the `active` you can use this endpoint for active/inactive toggle
- `DELETE /api/auth/crudzilla/users/{id}` this will delete your entity. 
- `GET /api/auth/crudzilla/users/{id}` return complete entity as a JSON object
- `GET /api/auth/crudzilla/users/all` return all entities in a JSON object. Obviously, this endpoint can be dangerous for tables that has more than a few entities. You can disable this endpoint with the *disableGetAll* property on the annotation.
- `GET /api/auth/crudzilla/users/autocomplete` autocomplete's are a pretty common operation for many CRUD's. Very often, you need to refer one entity on another entity CRUD. This endpoint is for autocomplete features on your frontend.
- `GET /api/auth/crudzilla/users/autocomplete/active` same as previous, but this only returns active entities.
- `GET /api/auth/crudzilla/users/autocomplete/ids` when you are loading a page that has autocompletes, usually you need to load the autocomplete record based only on id's.
- `GET /api/auth/crudzilla/users/multiselect` same as the autocomplete, but in a format for Multiselect components.
- `GET /api/auth/crudzilla/users/types` same as the autocomplete, but usually for Enum's that are listed on html selects.
- `GET /api/auth/crudzilla/users/types/multiselect` same as previous, but in a different format
- `GET /api/auth/crudzilla/users/projection/{projectionName}` this is used for projections over the user entity. More details below.

##Operations
Below, we will detail all the operations provided by CRUDZilla and how to implement/customize then. 

###Search `GET /api/auth/crudzilla/{key}`
Every CRUD need some search feature. Not only that, very often the user need's to select wich columns he can see, he can add Filters and sorting. Most CRUD's tools provide that, but CRUDZilla is here for you who need to control a little bit more of how these SQL's are produced.
In a large scale environment, you cannot page/sort/filter/showColumns in the frontend only. 
Also, you need to avoid unecessary JOIN's when you don't have some complex filters.

In order to achieve this, CRUDZilla uses QueryBuilders classes. The endpoint `GET /api/auth/crudzilla/users`, created for the User entity placed on `io.github.crudzilla.demo.users.domain.User` will try to find a `io.github.crudzilla.demo.users.app.UserQueryBuilder`

Your query builder will look something like this:

```java

@Service
public class UserQueryBuilder extends QueryBuilderJPASQL<UserFilter, UserDTO> {
    private static final SUser user = SUser.user; //This class is generated by the QueryDSL plugin

    @Override
    public JPASQLQuery<UserDTO> gerarQuery(UserFilter filter) {
        JPASQLQuery<UserDTO> query = new JPASQLQuery<>(entityManager, sqlTemplate);

        query.select(Projections.constructor(UserDTO.class,
                usuario.id,
                usuario.name,
                usuario.email,
                usuario.phone,
                usuario.active
        ));

        query.from(usuario);

        filterIfPresent(query, filter.getName(), () -> usuario.nomUsuario.contains(filter.getNome()));
        filterIfPresent(query, filter.getEmail(), () -> usuario.emlUsuario.contains(filter.getEmail()));
        filterIfNotEmpty(query, filter.getRoles(), () -> usuario.roleId.in(filter.getRoles()));
        filterIfPresent(query, filter.getAtivo(), () -> usuario.flgAtivo.eq(filter.getAtivo()));

        return query;
    }

    @Override
    public Expression<? extends Comparable> getOrderByExpression(String column) {
        return switch (column) {
            case "nome" -> usuario.nomUsuario;
            case "email" -> usuario.emlUsuario;
            case "cpf" -> usuario.cpfUsuario;
            default -> usuario.idUsuario;
        };
    }
}
```

Pretty straight-forward. With QueryBuilder, you have all the flexibility possible when creating your queries.
Also, because of QueryDSL, you have a Typed SQL queries, so you can catch errors on compile time.

There's a lot of static methods like `filterIfPresent` on the abstract QueryBuilderJPASQL class so you can do less coding.


###Save `POST /api/auth/crudzilla/{key}`

Soon...