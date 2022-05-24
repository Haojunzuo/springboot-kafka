package com.jh.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KafkaConsumer {
    @Autowired
    private ConsumerFactory consumerFactory;

    // 简单消费监听topic01
    @KafkaListener(topics = {"topic01"})
    // 消息转发至 topic02，由下面的监听topic02的函数负责进一步处理
    @SendTo("topic02")
    public String onMessage1(ConsumerRecord<?, ?> record){
        // 消费的哪个topic、partition的消息,打印出消息内容
        System.out.println("简单消费 topic01："+record.toString());
        return record.value()+"- forward message";
    }

    // 简单消费监听topic02
    @KafkaListener(topics = {"topic02"})
    public void onMessage2(ConsumerRecord<?, ?> record){
        // 消费的哪个topic、partition的消息,打印出消息内容
        System.out.println("简单消费 topic01->topic02: " + record.toString());
    }

    // 批量消费
//    @KafkaListener(groupId = "test", topics = "topic01",errorHandler = "consumerAwareErrorHandler")
//    public void onMessage3(List<ConsumerRecord<?, ?>> records) {
//        System.out.println(">>>批量消费一次，records.size()="+records.size());
//        for (ConsumerRecord<?, ?> record : records) {
//            System.out.println("record = " + record);
//            System.out.println(record.value());
//        }
//    }

    // 异常处理器，用@Bean注入
    @Bean
    public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler() {
        return (message, exception, consumer) -> {
            System.out.println("消费异常："+message.getPayload());
            return null;
        };
    }


    // 消息过滤器,用@Bean注入，只放行能被2整除的数，其他不予放行。
    @Bean
    public ConcurrentKafkaListenerContainerFactory filterContainerFactory() {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        // 被过滤的消息将被丢弃
        factory.setAckDiscarded(true);
        // 消息过滤策略
        factory.setRecordFilterStrategy(consumerRecord -> {
            return Integer.parseInt(consumerRecord.value().toString()) % 2 != 0;
            //返回true消息则被过滤
        });
        return factory;
    }

    // 将这个异常处理器的BeanName放到@KafkaListener注解的errorHandler属性里面
//    @KafkaListener(topics = {"topic02"},errorHandler = "consumerAwareErrorHandler",containerFactory = "filterContainerFactory")
    @KafkaListener(topics = {"topic02"},errorHandler = "consumerAwareErrorHandler")
    public void onMessage4(ConsumerRecord<?, ?> record) throws Exception {
        System.out.println("简单消费 topic01->topic02: " + record.toString());
        throw new Exception("简单消费-模拟异常");
    }
    // 批量消费也一样，异常处理器的message.getPayload()也可以拿到各条消息的信息
    @KafkaListener(topics = "topic",errorHandler="consumerAwareErrorHandler")
    public void onMessage5(List<ConsumerRecord<?, ?>> records) throws Exception {
        System.out.println("批量消费一次...");
        for (ConsumerRecord<?, ?> record : records) {
            System.out.println("record.value() = " + record.value());
        }
        throw new Exception("批量消费-模拟异常");
    }

}
