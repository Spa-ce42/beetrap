package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.rochester.beetrap.model.Flower;
import java.io.IOException;
import java.util.UUID;

public class FlowerDeserializer extends StdDeserializer<Flower> {

    public FlowerDeserializer() {
        this(null);
    }

    public FlowerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Flower deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper om = (ObjectMapper)p.getCodec();
        JsonNode flowerNode = om.readTree(p);
        JsonNode valuesNode = flowerNode.get("values");
        return new Flower(
                om.treeToValue(flowerNode.get("uuid"), UUID.class),
                valuesNode.get(0).asDouble(),
                valuesNode.get(1).asDouble(),
                valuesNode.get(2).asDouble(),
                valuesNode.get(3).asDouble(),
                valuesNode.get(4).asDouble()
        );
    }
}
