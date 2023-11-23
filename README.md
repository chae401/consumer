# 📌Kafka Consumer Server
> Kafka Broker의 'coffee' 토픽의 메시지를 구독하여 
처리하는 Consumer 서버 입니다.
>
> 해당 메시지(다회용기의 위치 및 상태 정보)를 WebSocket 통신을 통해 Client로 실시간으로 전달합니다.

## 설치 및 실행 방법
아래의 환경을 권장합니다.

| service | version |
|--------- |--------|
|**SpringBoot**|v3.1.x|
|**Java**|v17|
|**kafka_2.13**|v3.6.0|
|**spring-boot-starter-websocket**|v3.1.0|

## Kafka Consumer 구성
### Kafka Properties
```
@ConfigurationProperties("kafka")
@Data
public class KafkaProperties {
    public static final String CONSUMER_GROUP = "coffee-group";
    public static final String TOPIC = "coffee";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
}
```
- `CONSUMER_GROUP` : Kafka Consumer의 그룹 아이디를 나타내는 상수
- `TOPIC` : Kafka Broker에서 구독하는 토픽
- `bootstrapServers` : Kafka Broker의 부트스트랩 서버 주

### ConsumerConfiguration
```
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class ConsumerConfiguration {
    private final KafkaProperties kafkaProperties;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaProperties.CONSUMER_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }
}
```
- `kafkaListenerContainerFactory()` : Kafka Listener Container 생성
  - Kafka Consumer는 `ConcurrentMessageListenerContainer`를 사용하여 동시에 여러 메시지를 처리할 수 있도록 설정
- `consumerFactory()` : Kafka Consumer 생성
- `consumerConfigs()` : Kafka Consumer의 구성 속성 설정

## WebSocket 구성
### WebSocketConfiguration
```
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/container-status")
                .setAllowedOrigins("https://coffee-tree-front.web.app/", "http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```
- `registerStompEndpoints()` : WebSocket 엔드포인트를 등록
  - `addEndpoint("/container-status")`: "/container-status" 경로를 WebSocket 엔드포인트로 등록
  - `setAllowedOrigins`: WebSocket 연결을 허용할 원본(Origin)을 설정
  - `withSockJS` : SockJS를 사용하도록 설정 <br>
    - SockJS는 WebSocket이 지원되지 않는 환경에서 대체 수단을 제공하는 JavaScript 라이브러리입니다.
- `configureMessageBroker()` : 메시지 브로커를 구성하는 역할
  - `enableSimpleBroker("/topic")`: "/topic"을 구독하는 클라이언트에게 메시지를 전송하는 간단한 메시지 브로커를 활성화
  - `setApplicationDestinationPrefixes("/app")`: 클라이언트에서 메시지를 보낼 때 사용할 애플리케이션 프리픽스를 설정
    - 클라이언트는 "/app"을 접두사로 사용하여 메시지를 서버로 보낼 수 있습니다.
    - 현재 프로젝트에서는 사용되지 않는 설정입니다.
