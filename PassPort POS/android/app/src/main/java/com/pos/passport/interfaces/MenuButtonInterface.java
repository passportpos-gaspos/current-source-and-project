package com.pos.passport.interfaces;

import com.pos.passport.model.Cart;
import com.pos.passport.model.Product;

import java.util.List;

/**
 * Created by karim on 1/22/16.
 */
public interface MenuButtonInterface {
    void onNotifyQueueChanged();
    void onAddProduct(Product product);
    Cart getCart();
    List<Product> getProducts();
}
