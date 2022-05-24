//package com.jh.kafka.web;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
///**
// * kafka消费者测试
// */
//@Component
//public class TestConsumer {
//
//    @KafkaListener(topics = "topic01")
//    public void listen (ConsumerRecord<?, ?> record) throws Exception {
//        System.out.printf("topic = %s, partition = %s, offset = %d, value = %s \n", record.topic(), record.partition(), record.offset(), record.value());
//    }
//}
