package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.rochester.beetrap.model.Flower;
import java.io.IOException;

public class FlowerSerializer extends StdSerializer<Flower> {

    public FlowerSerializer() {
        this(null);
    }

    public FlowerSerializer(Class<Flower> t) {
        super(t);
    }

    @Override
    public void serialize(Flower value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("uuid", value.uuid());
        gen.writeArrayFieldStart("values");
        gen.writeNumber(value.v());
        gen.writeNumber(value.w());
        gen.writeNumber(value.x());
        gen.writeNumber(value.y());
        gen.writeNumber(value.z());
        gen.writeEndArray();
        gen.writeEndObject();
    }
}
