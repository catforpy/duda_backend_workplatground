package com.duda.file.provider.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ Producer 配置类
 * 参考老代码手动创建 DefaultMQProducer
 *
 * @author duda
 * @date 2026-03-14
 */
@Configuration
public class RocketMQProducerConfig {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Bean(destroyMethod = "shutdown")
    public DefaultMQProducer mqProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setRetryTimesWhenSendFailed(2);
        producer.setRetryTimesWhenSendAsyncFailed(2);
        producer.setRetryAnotherBrokerWhenNotStoreOK(true);

        producer.start();
        System.out.println();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   RocketMQ Producer 启动成功              ║");
        System.out.println("║   服务: duda-file-provider               ║");
        System.out.println("║   Group: " + producerGroup + "               ║");
        System.out.println("║   NameServer: " + nameServer + "    ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();

        return producer;
    }
}
