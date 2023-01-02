package com.example.localstackdemo.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.messaging.config.QueueMessageHandlerFactory;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.util.Collections;

@Configuration
public class SqsConfiguration {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.sqs.endpoint}")
    private String sqsEndpoint;

    @Value("${cloud.aws.queue.name}")
    private String queueName;

    @Bean
    //endpoint config for connecting to localstack and not actual aws environment.
    public AwsClientBuilder.EndpointConfiguration endpointConfiguration(){
        return new AwsClientBuilder.EndpointConfiguration(sqsEndpoint, Regions.SA_EAST_1.getName());
    }

    @Bean
    @Primary
    //This bean will be used for communicating to AWS SQS
    public AmazonSQSAsync amazonSQSAsync(final AwsClientBuilder.EndpointConfiguration endpointConfiguration){
        AmazonSQSAsync amazonSQSAsync = AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)
                ))
                .build();
        createQueues(amazonSQSAsync, queueName);
        return amazonSQSAsync;
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync){
        return new QueueMessagingTemplate(amazonSQSAsync);
    }

    //TODO: remover esse bind para criar a queue no start
    private void createQueues(final AmazonSQSAsync amazonSQSAsync,
                              final String queueName){
        amazonSQSAsync.createQueue(queueName);
        var queueUrl = amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
        amazonSQSAsync.purgeQueueAsync(new PurgeQueueRequest(queueUrl));
    }

    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory() {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setStrictContentTypeMatch(false);
        messageConverter.getObjectMapper().registerModule(new JavaTimeModule());
        factory.setMessageConverters(Collections.singletonList(messageConverter));
        return factory;
    }
}
