package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.rochester.beetrap.model.Garden;
import java.io.IOException;

public class GardenSerializer extends StdSerializer<Garden> {

    public GardenSerializer() {
        this(null);
    }

    public GardenSerializer(Class<Garden> t) {
        super(t);
    }

    @Override
    public void serialize(Garden garden, JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("uuid", garden.getUuid());
        jsonGenerator.writeStringField("name", garden.getName());
        jsonGenerator.writeObjectField("top_left", garden.getTopLeft());
        jsonGenerator.writeObjectField("bottom_right", garden.getBottomRight());
        jsonGenerator.writeObjectField("flowers", garden.getFlowers());
        jsonGenerator.writeEndObject();
    }
}
