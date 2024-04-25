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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.janilla.foodadvisor.api.Category;
import com.janilla.foodadvisor.api.Place;
import com.janilla.foodadvisor.api.Restaurant;
import com.janilla.foodadvisor.api.Restaurants;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.reflect.Flatten;
import com.janilla.reflect.Parameter;
import com.janilla.web.Handle;
import com.janilla.web.Render;

public class RestaurantsWeb {

	public static int PAGE_SIZE = 6;

	CustomPersistence persistence;

	public void setPersistence(CustomPersistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/restaurants")
	public Restaurants2 getRestaurants(@Parameter(name = "category") Long category,
			@Parameter(name = "place") Long place, @Parameter(name = "page") Integer page) throws IOException {
		var i = persistence.getCrud(Restaurants.class).find("slug", "restaurants");
		var s = i > 0 ? persistence.getCrud(Restaurants.class).read(i) : null;
		var n = page != null ? page : 1;
		var p = persistence
				.getCrud(Restaurant.class).filter(
						Map.of("category", category != null ? new Object[] { category } : new Object[0], "place",
								place != null ? new Object[] { place } : new Object[0]),
						(n - 1) * PAGE_SIZE, PAGE_SIZE);
		var cc = persistence.getCategories();
		var pp = persistence.getPlaces();
		var rr = persistence.getCrud(Restaurant.class).read(p.ids())
				.map(r -> new Restaurant2(r, cc.get(r.category), pp.get(r.place))).toList();
		return new Restaurants2(s, cc.values(), category, pp.values(), place, rr, n,
				(int) Math.ceilDiv(p.total(), PAGE_SIZE));
	}

	@Render(template = "Restaurants.html")
	public record Restaurants2(@Flatten Restaurants restaurants,
			Collection<@Render(template = "Restaurants-option.html") Category> categories, Long category,
			Collection<@Render(template = "Restaurants-option.html") Place> places, Long place, List<Restaurant2> items,
			int page, int lastPage) implements Renderer {

		public List<@Render(template = "Restaurants-button.html") Page> pages() {
			return List.of(new Page(page - 1, "Previous"), new Page(page + 1, "Next"));
		}

		@Override
		public boolean evaluate(RenderEngine engine) {
			record A(Category category, Object selected) {
			}
			record B(Place place, Object selected) {
			}
			record C(Page page, Object disabled) {
			}
			return engine.match(A.class, (i, o) -> {
				if (i.category.id.equals(category))
					o.setValue("selected");
			}) || engine.match(B.class, (i, o) -> {
				if (i.place.id.equals(place))
					o.setValue("selected");
			}) || engine.match(C.class, (i, o) -> {
				if (i.page.number < 1 || i.page.number > lastPage)
					o.setValue("disabled");
			});
		}
	}

	@Render(template = "Restaurants-item.html")
	public record Restaurant2(@Flatten Restaurant restaurant, Category category, Place place) {
	}

	public record Page(int number, String text) {
	}
}
