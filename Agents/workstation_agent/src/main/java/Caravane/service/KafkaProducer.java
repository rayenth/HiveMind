package Caravane.service;

import Caravane.events.FileChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service

public class KafkaProducer {



    @Autowired
    KafkaTemplate<String,String> kft;
    public void send(String topic, String msg){
        kft.send(topic,msg);
    }



}
