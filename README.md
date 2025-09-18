# laa-maat-scheduled-tasks

This is a Java 21 based Spring Boot application hosted on [MOJ Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/concepts/about-the-cloud-platform.html).

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Prerequisities
- Docker
- AWS CLI

## Building the application

To build the application run: 
```sh
./gradlew clean build
```

## Running the application locally

To run the application locally you will need to have docker installed and running.

You will also need to connect to the MAAT database. Instructions can be found [here](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/5900402794/Connecting+to+the+MAAT+Database).

### Obtaining environment variables for running locally

To run the app locally, you will need to download the appropriate environment variables from the team
vault in 1Password. These environment variables are stored as a .env file, which docker-compose uses
when starting up the service. If you don't see the team vault, speak to your tech lead to get access.

To begin with, make sure that you have the 1Password CLI installed:

```sh
op --version
```

If the command is not found, [follow the steps on the 1Password developer docs to get the CLI set-up](https://developer.1password.com/docs/cli/get-started/).

You can then run the start-local.sh script located in the maat-scheduled-tasks directory to 
pull down the env file and run the application.

```sh
./start-local.sh
```

## Testing the application locally

The postman collection for testing the endpoints set up for triggering the current scheduled tasks
can be found [here](https://dsdmoj.atlassian.net/wiki/spaces/ASLST/pages/5906956444/MST+Runbook).

Please update this whenever you add more scheduled tasks, along with the controllers to trigger them.

### Open-API docs
These can be found via localhost:8499/swagger-ui/index.html or by using the /swagger-ui/index.html
with one of the MST deployed environment URLs. 