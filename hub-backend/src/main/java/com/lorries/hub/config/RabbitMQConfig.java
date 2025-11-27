package com.lorries.hub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {

    // 交换机
    public static final String TRAFFIC_EXCHANGE = "traffic.exchange";
    public static final String ANOMALY_EXCHANGE = "anomaly.exchange";
    public static final String TASK_EXCHANGE = "task.exchange";

    // 队列
    public static final String TRAFFIC_FLOW_QUEUE = "traffic.flow.queue";
    public static final String ANOMALY_DETECTION_QUEUE = "anomaly.detection.queue";
    public static final String TASK_NOTIFICATION_QUEUE = "task.notification.queue";

    // 路由键
    public static final String TRAFFIC_FLOW_KEY = "traffic.flow";
    public static final String ANOMALY_DETECTION_KEY = "anomaly.detection";
    public static final String TASK_NOTIFICATION_KEY = "task.notification";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    // 交通流量交换机和队列
    @Bean
    public DirectExchange trafficExchange() {
        return new DirectExchange(TRAFFIC_EXCHANGE);
    }

    @Bean
    public Queue trafficFlowQueue() {
        return QueueBuilder.durable(TRAFFIC_FLOW_QUEUE).build();
    }

    @Bean
    public Binding trafficFlowBinding() {
        return BindingBuilder.bind(trafficFlowQueue()).to(trafficExchange()).with(TRAFFIC_FLOW_KEY);
    }

    // 异常检测交换机和队列
    @Bean
    public DirectExchange anomalyExchange() {
        return new DirectExchange(ANOMALY_EXCHANGE);
    }

    @Bean
    public Queue anomalyDetectionQueue() {
        return QueueBuilder.durable(ANOMALY_DETECTION_QUEUE).build();
    }

    @Bean
    public Binding anomalyDetectionBinding() {
        return BindingBuilder.bind(anomalyDetectionQueue()).to(anomalyExchange()).with(ANOMALY_DETECTION_KEY);
    }

    // 任务通知交换机和队列
    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE);
    }

    @Bean
    public Queue taskNotificationQueue() {
        return QueueBuilder.durable(TASK_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding taskNotificationBinding() {
        return BindingBuilder.bind(taskNotificationQueue()).to(taskExchange()).with(TASK_NOTIFICATION_KEY);
    }
}
