package restaurant.util;

import com.google.gson.*;
import restaurant.Drink;
import restaurant.Food;
import restaurant.Product;

import java.lang.reflect.Type;
import java.util.List;
import javafx.beans.property.Property;

public class JsonManager {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT)
            .setPrettyPrinting()
            .registerTypeAdapter(Product.class, new ProductAdapter())
            .create();

    public static String toJson(List<Product> products) {
        return gson.toJson(products);
    }

    public static Product[] fromJson(String json) {
        return gson.fromJson(json, Product[].class);
    }

    // Adaptor intern pentru a gestiona ierarhia Food/Drink în JSON
    private static class ProductAdapter implements JsonSerializer<Product>, JsonDeserializer<Product> {
        @Override
        public JsonElement serialize(Product src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = context.serialize(src).getAsJsonObject();
            result.addProperty("type", src instanceof Food ? "FOOD" : "DRINK");
            return result;
        }

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            if ("FOOD".equals(type)) return context.deserialize(json, Food.class);
            return context.deserialize(json, Drink.class);
        }
    }
}