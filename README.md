#  Customers Service - NTT Data Bootcamp (Avance 1)

Microservicio bancario desarrollado en el **Bootcamp de Microservicios**.  
Este repositorio corresponde al **Avance 1 del proyecto**, donde se implementa el microservicio de clientes.

---

## Alcance del Avance 1
En esta entrega se cumple con los siguientes requerimientos obligatorios del proyecto:

- Microservicio en **Java 11** con **Spring Boot WebFlux**
- Manejo de datos en **MongoDB** (ReactiveMongoRepository)
- CRUD completo de clientes (Create, Read, Update, Delete)
- Exposición de endpoints REST con nombres en inglés
- Configuración externalizada con **Spring Cloud Config Server**
- Contrato **OpenAPI 3.0** (Contract First)
- Uso de **Lombok** para reducir código repetitivo
- Manejo de logs con **Logback**
- Diagramas UML (arquitectura y secuencia de CRUD)

---

## Tecnologías utilizadas
- Java 11
- Spring Boot 2.7.x
- Spring WebFlux
- Spring Data MongoDB (reactivo)
- Spring Cloud Config
- OpenAPI 3.0 / Swagger
- Lombok
- Logback

---

## Diagramas UML

### Diagrama UML arquitectura
Muestra la interacción entre Clientes, Transacciones, Cuentas y Créditos.

![Diagrama de Arquitectura](docs/arquitectura.png)

###  Diagrama de Secuencia CRUD Clientes
Muestra el flujo de llamadas entre las capas (controller, service, repository, DB) para las operaciones CRUD.

![Diagrama de Secuencia CRUD](docs/crud-clientes.png)
