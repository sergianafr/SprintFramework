package src.mg.itu.prom16.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class GsonUtil {
    public static Gson createGson() {
        JsonDeserializer<Date> dateDeserializer = (json, typeOfT, context) -> {
            try {
                return Date.valueOf(json.getAsString());
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        };
        JsonSerializer<Date> dateSerializer = (src, typeOfSrc, context) ->
                new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(src));

        JsonDeserializer<Timestamp> timestampDeserializer = (json, typeOfT, context) -> {
            try {
                return Timestamp.valueOf(json.getAsString().replace("T", " "));
            } catch (Exception e) {
                throw new JsonParseException(e);
            }
        };
        JsonSerializer<Timestamp> timestampSerializer = (src, typeOfSrc, context) ->
                new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(src));

        return new GsonBuilder()
                .registerTypeAdapter(Date.class, dateSerializer)
                .registerTypeAdapter(Date.class, dateDeserializer)
                .registerTypeAdapter(Timestamp.class, timestampSerializer)
                .registerTypeAdapter(Timestamp.class, timestampDeserializer)
                .create();
    }
}
