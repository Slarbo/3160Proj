## ITCS 3160-0002, Spring 2024
## Marco Vieira, marco.vieira@charlotte.edu
## University of North Carolina at Charlotte

The code and resources provided are to be used only in the scope of ITCS 3160-0002, Spring 2024.
 
The main purpose is to provide examples on how to do the the initial setup of the database-centric REST API that must be developed for the course project.
In particular, the projects available are totally automated to be easily deployed in third-party setups with the help of a tool (in this case `docker` or maven, depending on the project). 

_This content does not replace the proper reading of the assignment description._


## Overview of the Contents

- [**`PostgreSQL`**](postgresql) - Database ready to run in a `docker` container with or without the help of the `docker-compose` tool;
- [**`Python`**](python) - Source code of web application template in python for execution  with `docker` container configured. Ready to run in `docker-compose` with PostgreSQL
  - [`app/`](python/app) folder is mounted to allow developing with container running
- [**`Java`**](java) - Source code of web application template in java/spark with `docker` container configured. Ready to run in `docker-compose` with PostgreSQL or in your favorite IDE.
- [**`postman`**](postman) - A collection of requests exported of postman tool;


## Requirements

To execute this project it is required to have installed:

- `docker`
- `docker-compose`
- `maven` only if you opt for the [java](java) option



## Demo [Python](python) REST API


To start this demo with run the script (e.g. [`sh docker-compose-python-psql.sh`](docker-compose-python-psql.sh)) to have both the server and the database running.
This script uses `docker-compose` and follows the configurations available in [`docker-compose-python-psql.yml`](docker-compose-python-psql.yml)).

The folder [`app`](python/app) is mapped into the container.
You can modify the contents and the server will update the sources without requiring to rebuild or restart the container.

* Web browser access: http://localhost:8080



## Demo [Java](java) REST API

To start this demo with run the script (e.g. [`sh docker-compose-java-psql.sh`](docker-compose-java-psql.sh)) to have both the server and the database running.

The demo available here uses [Spring Boot](https://spring.io/projects/spring-boot), which is one of the most widely used solutions to develop REST APIs and micro-services.
*"Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run."*
It is very easy to build and automate its deployment either with `maven` or `Docker`.

* Web browser access: http://localhost:8080


