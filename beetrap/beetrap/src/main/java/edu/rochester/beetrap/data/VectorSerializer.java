package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.bukkit.util.Vector;

public class VectorSerializer extends StdSerializer<Vector> {
    public VectorSerializer() {
        this(null);
    }

    public VectorSerializer(Class<Vector> t) {
        super(t);
    }

    @Override
    public void serialize(Vector vector, JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeNumber(vector.getX());
        jsonGenerator.writeNumber(vector.getY());
        jsonGenerator.writeNumber(vector.getZ());
        jsonGenerator.writeEndArray();
    }
}
