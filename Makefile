# Makefile — canonical entry point for building and running the stack.
# Run every target from the repo root. Compose is invoked with an explicit -f;
# the relative volume paths inside docker-compose.yml resolve against the compose
# file's own directory, so they still point at the repo-root target/ and logs/.

COMPOSE := docker compose -f docker/docker/docker-compose.yml

.DEFAULT_GOAL := help

.PHONY: help build up dev start redeploy down stop halt logs db-reset clean

help: ## Show available targets
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | \
		awk 'BEGIN{FS=":.*?## "}{printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

build: ## Build the WAR (target/docker-servlet.war)
	mvn clean package

up: ## Start colima + bring up the containers (detached)
	colima status >/dev/null 2>&1 || colima start --cpu 4 --memory 8
	$(COMPOSE) up -d

dev: build up ## Build then start the full stack
start: dev ## Alias for `dev`

redeploy: build ## Rebuild the WAR and restart Tomcat to pick it up
	docker restart tomcat-dev

down: ## Stop the containers
	$(COMPOSE) stop
stop: down ## Alias for `down`

halt: down ## Stop the containers and the colima VM
	colima stop

logs: ## Follow the Tomcat container logs
	docker logs -f tomcat-dev

db-reset: ## Restart Tomcat — the in-memory H2 DB is wiped and re-created by the listener
	docker restart tomcat-dev

clean: ## Remove Maven build output
	mvn clean
