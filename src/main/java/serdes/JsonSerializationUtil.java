package serdes;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public abstract class JsonSerializationUtil<T> implements Serializer<T>, Deserializer<T>, Serde<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonSerializationUtil() {
        OBJECT_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
    }

    public T deserialize(final String topic, final byte[] data, Class<T> tClass) {
        if (data == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(data, tClass);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (final Exception e) {
            throw new SerializationException("Error serializing JSON message", e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<T> serializer() {
        return this;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this;
    }
}



