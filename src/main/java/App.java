import java.util.Optional;
import java.util.Properties;

import entities.Hotel;
import entities.Weather;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.log4j.Logger;
import serdes.HotelSerializationUtil;
import serdes.WeatherSerializationUtil;

public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class);
    private static Properties config;
    private static final String WEATHER_TOPIC = "kafkaWeatherTopicWithGeohash";
    private static final String HOTEL_TOPIC = "kafkaHotelTopicWithGeohash";
    private static final String JOINED_TOPIC = "kafweather";

    static {
        config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "ks-hotels-app-id-x");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Optional.ofNullable(System.getenv("BOOTSTRAP_SERVERS_CONFIG")).orElse("sandbox-hdp.hortonworks.com:6667"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class.getName());
    }

    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, Hotel> hotelsTable = getHotelTable(builder);
        KStream<String, Weather> weatherStream = getWeatherStream(builder);
        Joined<String, Weather, Hotel> joined = Joined.with(Serdes.String(), new WeatherSerializationUtil(), new HotelSerializationUtil());

        KStream<String, Weather>[] joinedStreamWithPrecision5 = join(weatherStream, hotelsTable, joined);
        sendMatchedDataToTopic(joinedStreamWithPrecision5);
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
                LOGGER.info("Stream stopped");
            } catch (Exception exc) {
                LOGGER.error("Got exception while executing shutdown hook: ", exc);
            }
        }));
    }

    private static KTable<String, Hotel> getHotelTable(StreamsBuilder builder) {
        return hotelStreamToTable(builder
                .stream(HOTEL_TOPIC, Consumed.with(Serdes.String(), new HotelSerializationUtil()))
                .selectKey((key, value) -> value.getGeohash().substring(0, 5))
                .peek((key, value) -> LOGGER.info("Hotels table updated with key " + key))
        );
    }

    private static KTable<String, Hotel> hotelStreamToTable(KStream<String, Hotel> hotels) {
        return hotels
                .groupByKey(Serialized.with(Serdes.String(), new HotelSerializationUtil()))
                .reduce((aggV, newV) -> newV);
    }

    private static KStream<String, Weather> getWeatherStream(StreamsBuilder builder) {
        return builder
                .stream(WEATHER_TOPIC, Consumed.with(Serdes.String(), new WeatherSerializationUtil()))
                .selectKey((key, value) -> value.getGeohash().substring(0, 5))
                .peek((key, value) -> LOGGER.info("Weather stream processed record with key " + key));
    }

    private static KStream<String, Weather>[] join(KStream<String, Weather> weatherStream, KTable<String, Hotel> hotelsTable, Joined<String, Weather, Hotel> joined) {
        return weatherStream
                .selectKey((key, value) -> key.substring(0, 5))
                .leftJoin(hotelsTable, (weatherData, hotelData) -> {
                    weatherData.setId(hotelData.getId());
                    weatherData.setName(hotelData.getName());
                    weatherData.setPrecision(5);
                    weatherData.setGeohash(hotelData.getGeohash());
                    return weatherData;
                }, joined)
                .branch((key, value) -> value.getId() != null, (key, value) -> true);
    }

    private static void sendMatchedDataToTopic(KStream<String, Weather>[] hotelEnrichedData) {
        hotelEnrichedData[0]
                .peek((key, value) -> LOGGER.info("Joined with geohash: " + value.getGeohash()))
                .to(JOINED_TOPIC, Produced.with(Serdes.String(), new WeatherSerializationUtil()));
    }
}

