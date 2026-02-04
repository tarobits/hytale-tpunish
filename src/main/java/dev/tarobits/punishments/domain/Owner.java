package dev.tarobits.punishments.domain;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.tarobits.punishments.exceptions.DeveloperErrorException;

import java.util.UUID;

public record Owner(DomainObjectType type, UUID id) {
	public static Owner fromJson(JsonObject object) {
		try {
			DomainObjectType type = DomainObjectType.valueOf(object.get("type")
					                                                 .getAsString());
			UUID id = UUID.fromString(object.get("id")
					                          .getAsString());
			return new Owner(type, id);
		} catch (JsonParseException _) {
			throw new DeveloperErrorException("An error occurred while parsing owner.");
		}
	}

	public JsonObject toJsonObject() {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", type.name());
		obj.addProperty("id", id.toString());
		return obj;
	}
}
