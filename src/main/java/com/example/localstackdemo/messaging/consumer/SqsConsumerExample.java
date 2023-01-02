package com.example.localstackdemo.messaging.consumer;

import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class SqsConsumerExample {

    @SqsListener(value = "teste")
    public void consume(String message){
        System.out.println(">>>>>>>>>>>>>>>> Mensagem Recebida no SQS: "+message);
    }
}
