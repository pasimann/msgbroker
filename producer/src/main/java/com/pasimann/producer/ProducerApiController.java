package com.pasimann.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.json.JSONObject;
import org.json.JSONException;

import org.springframework.amqp.AmqpException;

import com.pasimann.producer.rabbitmq.MessageSender;
import com.pasimann.producer.dataobjects.ApplicationData;
import com.pasimann.producer.dataobjects.ResultType;

@Controller
public class ProducerApiController {

    private static final Logger log = LoggerFactory.getLogger(ProducerApiController.class);

    @Autowired
    MessageSender sender;

    @RequestMapping(value={"/send-message"}, method=RequestMethod.POST)
    public @ResponseBody ResponseEntity<ResultType> sendDataToStore(
        @RequestBody ApplicationData application) {

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();

        application.setId(randomUUIDString);
        application.setDate(new Date());

        log.info("REST API got message: {}", application.toString());
        log.info("Processing ID {}", randomUUIDString);

        try {
            String data = application.getData();
            JSONObject checkJsonFormat = new JSONObject(data);

            sender.sendMessageToQueue(application);
            log.info("Application {} sent to message queue.", randomUUIDString);

        } catch (JSONException e) {
            log.error("JSONException from data {}", application.toString());
            log.error("Caught JSONException from data.", e);
            return new ResponseEntity<ResultType>(
                new ResultType("503", "Invalid JSON format", null),
                HttpStatus.SERVICE_UNAVAILABLE);

        } catch (AmqpException  e) {
            log.error("AmqpException from data {}", application.toString());
            log.error("Caught Exception from the RabbitMq queue.", e);
            return new ResponseEntity<ResultType>(
                new ResultType("503", "RabbitMq Exception", null),
                HttpStatus.SERVICE_UNAVAILABLE);
        }

        return new ResponseEntity<ResultType>(
            new ResultType("200", "OK", randomUUIDString), HttpStatus.OK);
    }
}
