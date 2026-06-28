package com.cycleproject.b2b.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CartManager {
    private static final String PREF_NAME = "cart_prefs";
    private static final String KEY_CART = "cart_data";
    private final SharedPreferences prefs;
    private final Gson gson;

    public CartManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addToCart(long productId, int quantity) {
        Map<String, Integer> cart = getCart();
        String key = String.valueOf(productId);
        int currentQty = cart.getOrDefault(key, 0);
        cart.put(key, currentQty + quantity);
        saveCart(cart);
    }

    public Map<String, Integer> getCart() {
        String json = prefs.getString(KEY_CART, "");
        if (json.isEmpty()) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveCart(Map<String, Integer> cart) {
        String json = gson.toJson(cart);
        prefs.edit().putString(KEY_CART, json).apply();
    }

    public void clearCart() {
        prefs.edit().remove(KEY_CART).apply();
    }

    public int getCartCount() {
        return getCart().size();
    }
}
