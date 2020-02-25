package serdes;

import entities.Weather;

public class WeatherSerializationUtil extends JsonSerializationUtil<Weather> {

    @Override
    public Weather deserialize(String topic, byte[] data) {
        return deserialize(topic, data, Weather.class);
    }
}
