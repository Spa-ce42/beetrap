package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.bukkit.util.Vector;

public class VectorDeserializer extends StdDeserializer<Vector> {
    public VectorDeserializer() {
        this(null);
    }

    public VectorDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Vector deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return new Vector(node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble());
    }
}
