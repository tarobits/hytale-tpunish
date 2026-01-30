package dev.tarobits.punishments.utils.domainobject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.UUID;

public record Owner(DomainObjectType type, UUID id) {
    public JsonObject toJsonObject() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type.toJson());
        obj.addProperty("id", id.toString());
        return obj;
    }

    public static Owner fromJson(JsonObject object) {
        try {
            DomainObjectType type = DomainObjectType.fromJson(object.get("type").getAsString());
            UUID id = UUID.fromString(object.get("id").getAsString());
            return new Owner(type, id);
        } catch (JsonParseException _) {
            throw new IllegalArgumentException("An error occurred while parsing owner.");
        }
    }
}
