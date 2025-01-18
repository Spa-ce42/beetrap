package edu.rochester.beetrap.data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.rochester.beetrap.model.Garden;
import edu.rochester.beetrap.model.UUIDToFlowerMap;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.util.Vector;

public class GardenDeserializer extends StdDeserializer<Garden> {
    public GardenDeserializer() {
        this(null);
    }

    protected GardenDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Garden deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        ObjectMapper om = (ObjectMapper)jsonParser.getCodec();
        JsonNode node = om.readTree(jsonParser);
        JsonNode uuidNode = node.get("uuid");
        UUID uuid = om.treeToValue(uuidNode, UUID.class);
        String name = node.get("name").asText();
        JsonNode topLeftNode = node.get("top_left");
        JsonNode bottomRightNode = node.get("bottom_right");
        Vector topLeft = om.treeToValue(topLeftNode, Vector.class);
        Vector bottomRight = om.treeToValue(bottomRightNode, Vector.class);
        JsonNode flowersNode = node.get("flowers");
        UUIDToFlowerMap flowers = om.treeToValue(flowersNode, UUIDToFlowerMap.class);
        return new Garden(uuid, name, topLeft, bottomRight, flowers);
    }
}
