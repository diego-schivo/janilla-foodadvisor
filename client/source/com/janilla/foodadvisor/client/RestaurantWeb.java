/*
 * MIT License
 *
 * Copyright (c) 2024 Diego Schivo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.janilla.foodadvisor.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.janilla.foodadvisor.api.Category;
import com.janilla.foodadvisor.api.Place;
import com.janilla.foodadvisor.api.Restaurant;
import com.janilla.foodadvisor.api.Review;
import com.janilla.foodadvisor.api.User;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.reflect.Flatten;
import com.janilla.web.Handle;
import com.janilla.web.Render;

public class RestaurantWeb {

	CustomPersistence persistence;

	public void setPersistence(CustomPersistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/restaurants/([a-z-]+)")
	public Restaurant2 getRestaurant(String slug) throws IOException {
		var i = persistence.getCrud(Restaurant.class).find("slug", slug);
		var r = persistence.getCrud(Restaurant.class).read(i);
		var cc = persistence.getCategories();
		var pp = persistence.getPlaces();
		var ii = persistence.getCrud(Review.class).filter("restaurant", r.id);
		var rr = persistence.getCrud(Review.class).read(ii).toList();
		return new Restaurant2(r, cc.get(r.category), IntStream.rangeClosed(1, 5).boxed().toList(),
				rr.stream().mapToInt(x -> x.note).average().orElse(0),
				rr.size() == 1 ? "1 Review" : rr.size() + " Reviews",
				IntStream.range(0, r.price).mapToObj(x -> "€").collect(Collectors.joining(" ")), pp.get(r.place),
				rr.stream().map(x -> {
					User a;
					try {
						a = persistence.getCrud(User.class).read(x.author);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
					return new Review2(x, a);
				}).toList());
	}

	@Render(template = "Restaurant.html")
	public record Restaurant2(@Flatten Restaurant restaurant, Category category,
			List<@Render(template = "Restaurant-star.html") Integer> stars, double average, String reviewSummary,
			String price, Place place, List<Review2> reviews) implements Renderer {

		@Override
		public boolean evaluate(RenderEngine engine) {
			record A(Integer note, Object fill) {
			}
			return engine.match(A.class, (i, o) -> {
				o.setValue(i.note <= average ? "currentColor" : "none");
			});
		}
	}

	@Render(template = "Restaurant-review.html")
	public record Review2(@Flatten Review review, User author) {
	}
}
