package com.janilla.foodadvisor.api;

import com.janilla.persistence.Index;
import com.janilla.persistence.Store;
import com.janilla.reflect.Order;

@Store
public class Review {

	@Order(1)
	public Long id;

	@Order(2)
	public String content;

	@Order(3)
	public Integer note;

	@Order(4)
	@Reference(User.class)
	public Long author;

	@Index
	@Order(5)
	@Reference(Restaurant.class)
	public Long restaurant;
}
