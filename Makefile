# Self-Documented Makefile
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Start App Initially
initialize:  ## up db container & start app
	docker-compose down --rmi all --volumes --remove-orphans && \
	docker compose up -d db --build && \
	sleep 10 && \
	docker compose up -d api front --build
# Start App
start:  ## up db container & start app
	docker compose up -d
stop:  ## down containers
	docker compose down