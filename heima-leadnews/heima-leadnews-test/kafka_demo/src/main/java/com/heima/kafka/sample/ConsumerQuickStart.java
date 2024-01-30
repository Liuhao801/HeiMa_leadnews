package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class ConsumerQuickStart {
    public static void main(String[] args) {
        //1.添加kafka的配置信息
        Properties prop = new Properties();
        //kafka的连接地址
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "43.137.8.13:9092");
        //消费者组
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        //消息的反序列化器
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        //设置手动提交offset
        prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);

        //2.消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(prop);

        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-test"));

        //同步和异步组合提交偏移量
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record.value());
                    System.out.println(record.key());
                }
                consumer.commitAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("记录错误信息：" + e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }


//        while (true) {
//            //4.获取消息 每1000ms拉取一次
//            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
//            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
//                System.out.println(consumerRecord.key());
//                System.out.println(consumerRecord.value());
//                try {
//                    consumer.commitSync();//同步提交当前最新的偏移量
//                }catch (CommitFailedException e){
//                    System.out.println("记录提交失败的异常："+e);
//                }
//            }
//            //异步提交当前最新的偏移量
//            consumer.commitAsync(new OffsetCommitCallback() {
//                @Override
//                public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
//                    if(e!=null){
//                        System.out.println("记录错误的提交偏移量："+ map+",异常信息"+e);
//                    }
//                }
//            });
//        }
    }
}
