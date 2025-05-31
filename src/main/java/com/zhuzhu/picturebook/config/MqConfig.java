package com.zhuzhu.picturebook.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sound.midi.Receiver;
@Component
public class MqConfig {

   public static final String topicExchangeName = "PICTURE_GEN_IMG_EXCHANGE";
    public static final String GEN_QUEUE_AME = "PICTURE_GEN_IMG_QUEUE";
   public static final String GEN_DONE_QUEUE_AME = "PICTURE_GEN_IMG_DONE_QUEUE";
    public static final String ROUTING_KEY_GEN = "picture.routing.gen";
    public static final String ROUTING_KEY_DONE = "picture.routing.done";

    @Bean
    Queue genQueue() {
        return QueueBuilder.durable(GEN_QUEUE_AME).build();
    }
    @Bean
    Queue genDoneQueue() {
        return QueueBuilder.durable(GEN_DONE_QUEUE_AME).build();
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(topicExchangeName,true,false);
    }

    @Bean
    Binding bindingGen(Queue genQueue, DirectExchange exchange) {
        return BindingBuilder.bind(genQueue).to(exchange).with(ROUTING_KEY_GEN);
    }
    @Bean
    Binding bindingGenDone(Queue genDoneQueue, DirectExchange exchange) {
        return BindingBuilder.bind(genDoneQueue).to(exchange).with(ROUTING_KEY_DONE);
    }



}
