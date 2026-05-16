DC = docker-compose
PORTS = 1883 5672 15672 8080 8081 8082 8083

kill:
	fuser -k $(PORTS) 2>/dev/null; echo OK

down:
	$(DC) down --remove-orphans 2>/dev/null; echo OK

clean: kill down

up: clean
	$(DC) up -d --force-recreate

rebuild: clean
	$(DC) build --no-cache; $(DC) up -d --force-recreate

rabbit: kill
	$(DC) up -d --force-recreate rabbitmq; sleep 8

mosquitto:
	$(DC) up -d --force-recreate mosquitto; sleep 3

bridge: kill
	$(DC) up -d --force-recreate rabbitmq mqtt-bridge; sleep 8; docker logs mqtt_rabbitmq_bridge --tail 5

api:
	$(DC) up -d --force-recreate fleet-api

gps:
	$(DC) up -d --force-recreate fleet-gps-service

alert:
	$(DC) up -d --force-recreate fleet-alert-service

notif:
	$(DC) up -d --force-recreate fleet-notification-service

start: rabbit mosquitto bridge api gps alert notif

logs-rabbit:
	docker logs rabbitmq_broker --tail 20

logs-bridge:
	docker logs mqtt_rabbitmq_bridge --tail 20

ps:
	$(DC) ps
