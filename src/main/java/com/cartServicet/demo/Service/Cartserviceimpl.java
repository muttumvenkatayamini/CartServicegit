package com.cartServicet.demo.Service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 
 * @author muttum.venkatayamini
 *
 */
import com.cartServicet.demo.CartDto.CartDtoResponse;
import com.cartServicet.demo.CartDto.Cartdto;
import com.cartServicet.demo.CartDto.DeleteProductDto;
import com.cartServicet.demo.CartDto.ProductDto;
import com.cartServicet.demo.CartDto.ProductResponseDto;
import com.cartServicet.demo.Entity.CartEntity;
import com.cartServicet.demo.Entity.ProductEntity;
import com.cartServicet.demo.repository.CartRepo;
import com.cartServicet.demo.repository.ProductRepo;

@Service
public class Cartserviceimpl implements CartService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private ProductRepo productRepo;

	@Override

	public CartDtoResponse createCart(Cartdto cartDto) {
		Optional<CartEntity> existingCart = cartRepo.findByUserId(cartDto.getUserId());
		if (existingCart.isPresent()) {
			throw new RuntimeException("User already has a cart, Cart Id : " +existingCart.get().getCartId());
		}else {
			CartEntity cart = new CartEntity();
			cart.setUserId(cartDto.getUserId());
			cartRepo.save(cart);
			CartDtoResponse cartResponse = new CartDtoResponse();
			cartResponse.setResponse("Cart Id: " + cart.getCartId());
			cartResponse.setCartId(cart.getCartId());
			return cartResponse;
		}
		

	}

	@Override
	public ProductResponseDto addProductToCart(ProductDto productDto) {
		ProductEntity product = new ProductEntity();
		CartEntity cart = cartRepo.findById(productDto.getCartId())
				.orElseThrow(() -> new RuntimeException("cart not found"));

		product.setCartEntity(cart);
		product.setPrice(productDto.getPrice());
		product.setProductid(productDto.getProductId());
		product.setQuantity(productDto.getQuantity());
		productRepo.save(product);

		double totalPrice = cart.getTotalPrice() + (productDto.getPrice() * productDto.getQuantity());
		cart.setTotalPrice(totalPrice);
		cartRepo.save(cart);

		ProductResponseDto productResponse = new ProductResponseDto();
		productResponse.setProductId(product.getProductid());
		productResponse.setPrice(product.getPrice());
		productResponse.setQuantity(product.getQuantity());
		return productResponse;
	}

	@Override

	public void deleteCart(Long cartId) {
		CartEntity cart = cartRepo.findById(cartId).orElseThrow(() -> new RuntimeException("Cart not found"));
		// Delete associated products or update their references
		cartRepo.deleteById(cartId);

	}

	@Override
	public List<ProductEntity> getAllProductsByCartId(Long cartId) {
		List<ProductEntity> requests = productRepo.findAllProductEntityByCartEntity_cartId(cartId);
		return requests;
	}

	@Override
	public void deleteProduct(DeleteProductDto deleteProductDto) {

		ProductEntity product = productRepo.findById(deleteProductDto.getProductId())
				.orElseThrow(() -> new RuntimeException("Product not found"));
		CartEntity cart = cartRepo.findById(deleteProductDto.getCartId())
				.orElseThrow(() -> new RuntimeException("cart not found"));
		productRepo.deleteById(deleteProductDto.getProductId());

		double totalPrice = cart.getTotalPrice() - (product.getPrice() * product.getQuantity());
		cart.setTotalPrice(totalPrice);
		cartRepo.save(cart);

	}

}
