package com.janilla.foodadvisor.client;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import com.janilla.foodadvisor.api.Category;
import com.janilla.foodadvisor.api.Place;
import com.janilla.persistence.Persistence;

public class CustomPersistence extends Persistence {

	public Map<Long, Category> getCategories() throws IOException {
		var c = getCrud(Category.class);
		var s = c.read(c.list());
		return s.collect(Collectors.toMap(x -> x.id, x -> x));
	}

	public Map<Long, Place> getPlaces() throws IOException {
		var c = getCrud(Place.class);
		var s = c.read(c.list());
		return s.collect(Collectors.toMap(x -> x.id, x -> x));
	}
}
