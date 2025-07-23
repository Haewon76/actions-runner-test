package com.cashmallow.api.config;

import com.cashmallow.api.infrastructure.rabbitmq.RabbitReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
public class RabbitConfig {

    private static final String EXCHANGE_TOPIC_EXCHANGE = "exchange";
    private static final String CASHOUT_TOPIC_EXCHANGE = "cashout";
    private static final String CURRENCY_TOPIC_EXCHANGE = "currency";
    private static final String NOTIFICATION_TOPIC_EXCHANGE = "notification";
    private static final String USER_TOPIC_EXCHANGE = "user";
    private static final String BANK_ACCOUNT_TOPIC_EXCHANGE = "bankaccount";
    private static final String REMITTANCE_TOPIC_EXCHANGE = "remittance";
    private static final String WALLET_TOPIC_EXCHANGE = "wallet";
    private static final String REFUND_TOPIC_EXCHANGE = "refund";
    private static final String MONEY_STATISTICS_TOPIC_EXCHANGE = "money-statistics";
    private static final String COUPON_TOPIC_EXCHANGE = "coupon";
    private static final String GLOBAL_TRAVELER_EDD_TOPIC_EXCHANGE = "global-traveler-edd";

    public static final String AUTHME_TIMEOUT_TOPIC = "message-delay-authme";
    public static final String AUTHME_TIMEOUT_ROUTING_KEY = "cmd.authme.timeout";
    public static final String AUTHME_TIMEOUT_QUEUE = "message-delay-authme-queue";

    public static final String CERTIFICATION_TIMEOUT_TOPIC = "message-delay-certification";
    public static final String CERTIFICATION_TIMEOUT_ROUTING_KEY = "cmd.certification.timeout";
    public static final String CERTIFICATION_TIMEOUT_QUEUE = "message-delay-certification-queue";

    public static final String CERTIFICATION_STEP_TIMEOUT_ROUTING_KEY = "cmd.certification.step.timeout";
    public static final String CERTIFICATION_STEP_TIMEOUT_QUEUE = "message-delay-certification-step-queue";

    public static final String DELAY_TEST_TOPIC = "message-delay-exchange";
    public static final String DELAY_TEST_ROUTING_KEY = "delay-routingkey";
    public static final String DELAY_TEST_QUEUE = "message-delay-queue";


    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.port}")
    private Integer port;
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.listener.simple.retry.initial-interval}")
    private Integer initialInterval;
    @Value("${rabbitmq.listener.simple.retry.max-interval}")
    private Integer maxInterval;
    @Value("${rabbitmq.listener.simple.retry.multiplier}")
    private Integer multiplier;

    @Value("${rabbitmq.listener.simple.retry.max-attempts}")
    private Integer maxAttempts;

    // RetryTemplate
    @Bean
    public RetryTemplate retryTemplate() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean
    public RabbitReceiver rabbitReceiveService() {
        return new RabbitReceiver();
    }

    @Bean
    public RabbitReceiver recoverer() {
        return new RabbitReceiver();
    }

    // Retry Interceptor
    @Bean
    public StatelessRetryOperationsInterceptorFactoryBean retryInterceptor() {
        StatelessRetryOperationsInterceptorFactoryBean retryInterceptor = new StatelessRetryOperationsInterceptorFactoryBean();
        retryInterceptor.setRetryOperations(retryTemplate());
        retryInterceptor.setMessageRecoverer(recoverer());
        return retryInterceptor;
    }

    // Rabbit Template
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setRetryTemplate(retryTemplate());
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate())
                .recoverer(recoverer())
                .build());
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    // Topic Exchange
    @Bean
    public TopicExchange exchangeTopicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicRemittance() {
        return new TopicExchange(REMITTANCE_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicWallet() {
        return new TopicExchange(WALLET_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicRefund() {
        return new TopicExchange(REFUND_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicCashout() {
        return new TopicExchange(CASHOUT_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicCurrency() {
        return new TopicExchange(CURRENCY_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicNotification() {
        return new TopicExchange(NOTIFICATION_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicUser() {
        return new TopicExchange(USER_TOPIC_EXCHANGE);
    }

    // @Bean
    // public CustomExchange authmeTopicTimeout() {
    //     Map<String, Object> args = new HashMap<>();
    //     args.put("x-delayed-type", "direct");
    //     return new CustomExchange(AUTHME_TIMEOUT_TOPIC, "x-delayed-message", true, false, args);
    // }

    @Bean
    public TopicExchange exchangeTopicBankAccount() {
        return new TopicExchange(BANK_ACCOUNT_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicMoneyStatistics() {
        return new TopicExchange(MONEY_STATISTICS_TOPIC_EXCHANGE);
    }

    @Bean
    public TopicExchange exchangeTopicCoupon() {
        return new TopicExchange(COUPON_TOPIC_EXCHANGE);
    }

    // Queue config
    @Bean
    public Queue exchangeCancelQueue() {
        return new Queue("exchange-cancel-queue");
    }

    @Bean
    public Queue expireWalletQueue() {
        return new Queue("wallet-expire-queue");
    }

    @Bean
    public Queue standbyRefundOfExpiredWalletQueue() {
        return new Queue("refund-expire-wallet-queue");
    }

    @Bean
    public Queue requestRefundForStandbyQueue() {
        return new Queue("request-refund-standby-queue");
    }

    @Bean
    public Queue notifyWalletExpireBefore7DayQueue() {
        return new Queue("wallet-expire-before-7day-queue");
    }

    @Bean
    public Queue remittanceCancelQueue() {
        return new Queue("remittance-cancel-queue");
    }

    @Bean
    public Queue cashoutCancelQueue() {
        return new Queue("cashout-cancel-queue");
    }

    @Bean
    public Queue currencyCollectQueue() {
        return new Queue("currency-collect-queue");
    }

    @Bean
    public Queue notificationDeleteQueue() {
        return new Queue("notification-delete-queue");
    }

    @Bean
    public Queue userDormantQueue() {
        return new Queue("user-dormant-queue");
    }

    @Bean
    public Queue moneyStatisticsQueue() {
        return new Queue("money-statistics-collect-queue");
    }

    @Bean
    public Queue couponIssueSystemQueue() {
        return new Queue("coupon-issue-system-queue");
    }

    @Bean
    public Queue couponUpdateExpireQueue() {
        return new Queue("coupon-update-expire-queue");
    }

    @Bean
    public Queue couponPushExpireQueue() {
        return new Queue("coupon-push-expire-queue");
    }

    @Bean
    public Queue calculateUnpaidQueue() {
        return new Queue("calculate-unpaid-queue");
    }

    // Create binding
    @Bean
    public Binding bindingExchange(TopicExchange exchangeTopicExchange, Queue exchangeCancelQueue) {
        return BindingBuilder.bind(exchangeCancelQueue)
                .to(exchangeTopicExchange)
                .with("exchange.cmd.cancel");
    }

    @Bean
    public Binding bindingWalletExpire(TopicExchange exchangeTopicWallet, Queue expireWalletQueue) {
        return BindingBuilder.bind(expireWalletQueue)
                .to(exchangeTopicWallet)
                .with("wallet.cmd.expire");
    }

    @Bean
    public Binding bindingRefundOfExpiredWallet(TopicExchange exchangeTopicWallet, Queue standbyRefundOfExpiredWalletQueue) {
        return BindingBuilder.bind(standbyRefundOfExpiredWalletQueue)
                .to(exchangeTopicWallet)
                .with("wallet.cmd.standby.refund");
    }

    @Bean
    public Binding bindingRequestRefund(TopicExchange exchangeTopicRefund, Queue requestRefundForStandbyQueue) {
        return BindingBuilder.bind(requestRefundForStandbyQueue)
                .to(exchangeTopicRefund)
                .with("refund.cmd.request");
    }

    @Bean
    public Binding bindingNotifyWalletExpireBefore7Day(TopicExchange exchangeTopicWallet, Queue notifyWalletExpireBefore7DayQueue) {
        return BindingBuilder.bind(notifyWalletExpireBefore7DayQueue)
                .to(exchangeTopicWallet)
                .with("wallet.cmd.expire.before");
    }

    @Bean
    public Binding bindingRemittanceCancel(TopicExchange exchangeTopicRemittance, Queue remittanceCancelQueue) {
        return BindingBuilder.bind(remittanceCancelQueue)
                .to(exchangeTopicRemittance)
                .with("remittance.cmd.cancel");
    }

    @Bean
    public Binding bindingCashout(TopicExchange exchangeTopicCashout, Queue cashoutCancelQueue) {
        return BindingBuilder.bind(cashoutCancelQueue)
                .to(exchangeTopicCashout)
                .with("cashout.cmd.cancel");
    }

    @Bean
    public Binding bindingCurrency(TopicExchange exchangeTopicCurrency, Queue currencyCollectQueue) {
        return BindingBuilder.bind(currencyCollectQueue)
                .to(exchangeTopicCurrency)
                .with("currency.cmd.collect");
    }

    @Bean
    public Binding bindingNotification(TopicExchange exchangeTopicNotification, Queue notificationDeleteQueue) {
        return BindingBuilder.bind(notificationDeleteQueue)
                .to(exchangeTopicNotification)
                .with("notification.cmd.delete");
    }

    @Bean
    public Binding bindingUser(TopicExchange exchangeTopicUser, Queue userDormantQueue) {
        return BindingBuilder.bind(userDormantQueue)
                .to(exchangeTopicUser)
                .with("user.cmd.dormant");
    }

    // @Bean
    // public Binding bindingAuthmeTimeout(CustomExchange authmeTopicTimeout, Queue userDormantQueue) {
    //     return BindingBuilder.bind(userDormantQueue)
    //             .to(authmeTopicTimeout)
    //             .with(AUTHME_TIMEOUT_ROUTING_KEY);
    // }

    @Bean
    public Binding bindingMoneyStatistics(TopicExchange exchangeTopicMoneyStatistics, Queue moneyStatisticsQueue) {
        return BindingBuilder.bind(moneyStatisticsQueue)
                .to(exchangeTopicMoneyStatistics)
                .with("money-statistics.cmd.collect");
    }

    @Bean
    public Binding bindingCouponIssueSystem(TopicExchange exchangeTopicCoupon, Queue couponIssueSystemQueue) {
        return BindingBuilder.bind(couponIssueSystemQueue)
                .to(exchangeTopicCoupon)
                .with("coupon.cmd.system");
    }

    @Bean
    public Binding bindingCouponUpdateExpire(TopicExchange exchangeTopicCoupon, Queue couponUpdateExpireQueue) {
        return BindingBuilder.bind(couponUpdateExpireQueue)
                .to(exchangeTopicCoupon)
                .with("coupon.cmd.expire");
    }

    @Bean
    public Binding bindingCouponPushExpire(TopicExchange exchangeTopicCoupon, Queue couponPushExpireQueue) {
        return BindingBuilder.bind(couponPushExpireQueue)
                .to(exchangeTopicCoupon)
                .with("coupon.cmd.push");
    }

    @Bean
    public Binding bindingCalculateUnpaid(TopicExchange exchangeTopicMoneyStatistics, Queue calculateUnpaidQueue) {
        return BindingBuilder.bind(calculateUnpaidQueue)
                .to(exchangeTopicMoneyStatistics)
                .with("money-statistics.cmd.unpaid");
    }

    /**
     * Creates a delayed exchange with the name "message-delay-exchange" and type "x-delayed-message".
     *
     * @return the created CustomExchange object
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        // exchange name: "message-delay-exchange" 변경해서 사용
        return new CustomExchange(DELAY_TEST_TOPIC, "x-delayed-message", true, false, args);
    }

    @Bean
    public CustomExchange delayedAuthme() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        // exchange name: "message-delay-exchange" 변경해서 사용
        return new CustomExchange(AUTHME_TIMEOUT_TOPIC, "x-delayed-message", true, false, args);
    }

    @Bean
    public CustomExchange delayedCertification() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        // exchange name: "message-delay-exchange" 변경해서 사용
        return new CustomExchange(CERTIFICATION_TIMEOUT_TOPIC, "x-delayed-message", true, false, args);
    }

    /**
     * Creates and returns a new instance of the Binding class with the given parameters.
     * The Queue object provided is bound to the CustomExchange object using the provided routing
     * key "delay-routingkey" and no additional arguments.
     *
     * @param messageDelayQueue the Queue object to be bound
     * @param delayedExchange   the CustomExchange object to bind the Queue to
     * @return the newly created Binding object
     */
    @Bean
    public Binding binding(Queue messageDelayQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(messageDelayQueue).to(delayedExchange).with(DELAY_TEST_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding bindingAuthme(Queue authmeTimeoutDelayQueue, CustomExchange delayedAuthme) {
        return BindingBuilder.bind(authmeTimeoutDelayQueue).to(delayedAuthme).with(AUTHME_TIMEOUT_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding bindingCertification(Queue certificationTimeoutDelayQueue, CustomExchange delayedCertification) {
        return BindingBuilder.bind(certificationTimeoutDelayQueue).to(delayedCertification).with(CERTIFICATION_TIMEOUT_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding bindingDelayCertificationStep(Queue certificationStepTimeoutDelayQueue, CustomExchange delayedCertification) {
        return BindingBuilder.bind(certificationStepTimeoutDelayQueue).to(delayedCertification)
                .with(CERTIFICATION_STEP_TIMEOUT_ROUTING_KEY).noargs();
    }

    /**
     * Creates and returns a new instance of the Queue class with the specified name.
     * The created Queue object represents a message delay queue.
     *
     * @return the newly created Queue object
     */
    @Bean
    public Queue messageDelayQueue() {
        return new Queue(DELAY_TEST_QUEUE, true);
    }

    @Bean
    public Queue authmeTimeoutDelayQueue() {
        return new Queue(AUTHME_TIMEOUT_QUEUE, true);
    }

    @Bean
    public Queue certificationTimeoutDelayQueue() {
        return new Queue(CERTIFICATION_TIMEOUT_QUEUE, true);
    }

    @Bean
    public Queue certificationStepTimeoutDelayQueue() {
        return new Queue(CERTIFICATION_STEP_TIMEOUT_QUEUE, true);
    }

    /**
     * Returns a new instance of the TopicExchange class representing the global EDD topic exchange.
     * The exchange name is set to GLOBAL_TRAVELER_EDD_TOPIC_EXCHANGE.
     *
     * @return a new instance of the TopicExchange class representing the global EDD topic exchange
     */
    @Bean
    public TopicExchange globalEddTopicExchange() {
        return new TopicExchange(GLOBAL_TRAVELER_EDD_TOPIC_EXCHANGE);
    }
}
