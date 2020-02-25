package serdes;

import entities.Hotel;

public class HotelSerializationUtil extends JsonSerializationUtil<Hotel> {

    @Override
    public Hotel deserialize(String topic, byte[] data) {
        return deserialize(topic, data, Hotel.class);
    }
}
