# Message Broker queue

## General information

Simple spring boot application. Sends data to RabbitMq store queue.

## Running

```
docker-compose build
docker-compose up -d
docker-compose ps
# tail logs
docker-compose logs -f consumer
# log into docker volume
docker exec -it consumer bash
```

## Testing

Use curl to test send some JSON data from producer via RabbitMq queue to Consumer.

```
curl -i -H "Content-Type:application/json" localhost:8080/send-message -d '{"data":"{firstName: Foo lastName: Bar}"}'
```
