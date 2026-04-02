package com.learning_engine.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig
{
    @Value("${rabbitmq.exchanges.enrollments}")
    private String enrollmentsExchange;

    @Value("${rabbitmq.exchanges.modules}")
    private String modulesExchange;

    @Value("${rabbitmq.queues.enrollment-activated}")
    private String enrollmentActivatedQueue;

    @Value("${rabbitmq.queues.module-completed}")
    private String moduleCompletedQueue;

    @Value("${rabbitmq.routing-keys.enrollment-activated}")
    private String enrollmentActivatedKey;

    @Value("${rabbitmq.routing-keys.module-completed}")
    private String moduleCompletedKey;

    @Bean
    public TopicExchange enrollmentsExchange(){
        return new TopicExchange(enrollmentsExchange);
    }

    @Bean
    public TopicExchange modulesExchange(){
        return new TopicExchange(modulesExchange);
    }

    @Bean
    public Queue enrollmentActivatedQueue(){
        return QueueBuilder.durable(enrollmentActivatedQueue).build();
    }

    @Bean
    public Queue moduleCompletedQueue(){
        return QueueBuilder.durable(moduleCompletedQueue).build();
    }

    @Bean
    public Binding enrollmentActivatedBinding (){
        return BindingBuilder
                .bind(enrollmentActivatedQueue())
                .to(enrollmentsExchange())
                .with(enrollmentActivatedKey);
    }

    @Bean
    public Binding moduleCompletedBinding (){
        return BindingBuilder
                .bind(moduleCompletedQueue())
                .to(modulesExchange())
                .with(moduleCompletedKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
