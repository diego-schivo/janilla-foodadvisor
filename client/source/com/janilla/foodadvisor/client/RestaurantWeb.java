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

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.janilla.foodadvisor.api.Asset;
import com.janilla.foodadvisor.api.Category;
import com.janilla.foodadvisor.api.Restaurant;
import com.janilla.foodadvisor.api.Review;
import com.janilla.foodadvisor.api.User;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Flatten;
import com.janilla.web.Handle;
import com.janilla.web.NotFoundException;
import com.janilla.web.Render;

public class RestaurantWeb {

	private static DecimalFormat noteFormatter = new DecimalFormat("0.#");

	public FoodAdvisorClientApp.Persistence persistence;

	@Handle(method = "GET", path = "/restaurants/([a-z-]+)")
	public Restaurant2 getRestaurant(String slug) {
		var i = persistence.getCrud(Restaurant.class).find("slug", slug);
		var r = i > 0 ? persistence.getCrud(Restaurant.class).read(i) : null;
		if (r == null)
			throw new NotFoundException();
		return Restaurant2.of(r, persistence);
	}

	@Render(template = "Restaurant.html")
	public record Restaurant2(@Flatten Restaurant restaurant, Gallery gallery, Category category, String reviewSummary,
			String price, Double average,
			List<Map.@Render(template = "Restaurant-note.html") Entry<String, Integer>> notes,
			@Render(template = "Restaurant-reviews.html") List<Review2> reviews, RelatedRestaurants2 relatedRestaurants)
			implements Renderer {

		public static Restaurant2 of(Restaurant restaurant, FoodAdvisorClientApp.Persistence persistence) {
			if (restaurant == null)
				return null;
			var cc = persistence.getCategories();
			var c = cc.get(restaurant.category());
			var ii = persistence.getCrud(Review.class).filter("restaurant", restaurant.id());
			var rr = persistence.getCrud(Review.class).read(ii).toList();
			var s = switch (rr.size()) {
			case 0 -> null;
			case 1 -> "1 Review";
			default -> rr.size() + " Reviews";
			};
			var p = IntStream.range(0, restaurant.price()).mapToObj(x -> "€").collect(Collectors.joining(" "));
			var a = !rr.isEmpty() ? Double.valueOf(rr.stream().mapToInt(x -> x.note()).average().orElseThrow()) : null;
			var qq = !rr.isEmpty() ? rr.stream().map(x -> Review2.of(x, persistence)).toList() : null;
			List<Map.Entry<String, Integer>> nn;
			{
				var kk = new String[] { "Excellent", "Good", "Average", "Bellow Average", "Poor" };
				var vv = new int[5];
				for (var x : rr)
					vv[5 - x.note()]++;
				nn = IntStream.range(0, kk.length).mapToObj(x -> Map.entry(kk[x], vv[x])).toList();
			}
			return new Restaurant2(restaurant, Gallery.of(restaurant.images(), persistence), c, s, p, a, nn, qq,
					RelatedRestaurants2.of(restaurant.relatedRestaurants(), persistence));
		}

		public String className() {
			return "restaurant";
		}

		public @Render(template = "Restaurant-stars.html") List<@Render(template = "Restaurant-stars-item.html") Integer> stars() {
			return average != null ? IntStream.rangeClosed(1, 5).boxed().toList() : null;
		}

		public String averageSummary() {
			return noteFormatter.format(average) + "/5";
		}

		@Override
		public boolean evaluate(RenderEngine engine) {
			record A(Integer note, Object fill) {
			}
			record B(Restaurant.Location location, Object items) {
			}
			record C(Map.Entry<String, Integer> note, Object style) {
			}
			return engine.match(A.class, (i, o) -> {
				o.setValue(i.note <= average ? "currentColor" : "none");
			}) || engine.match(B.class, (i, o) -> {
				var l = i.location;
				o.setValue(Stream.of(l.address(), l.website(), l.phone()).filter(x -> x != null).toList());
			}) || engine.match(C.class, (i, o) -> {
				o.setValue("style=\"width: calc(100% / " + reviews.size() + " * " + i.note.getValue() + ")\"");
			});
		}
	}

	@Render(template = "Restaurant-review.html")
	public record Review2(@Flatten Review review, User2 author, String timeSummary) implements Renderer {

		public static Review2 of(Review review, Persistence persistence) {
			if (review == null)
				return null;
			var u = persistence.getCrud(User.class).read(review.author());
			var d = Duration.between(review.creationTime(), Instant.now());
			var l = d.toDaysPart();
			String t;
			if (l > 0)
				t = (l == 1 ? "1 day" : l + " days") + " ago";
			else {
				l = d.toHoursPart();
				if (l > 0)
					t = (l == 1 ? "1 hour" : l + " hours") + " ago";
				else {
					l = d.toMinutesPart();
					if (l > 0)
						t = (l == 1 ? "1 minute" : l + " minutes") + " ago";
					else {
						l = d.toSecondsPart();
						t = (l == 1 ? "1 second" : l + " seconds") + " ago";
					}
				}
			}
			return new Review2(review, User2.of(u, persistence), t);
		}

		public @Render(template = "Restaurant-stars.html") List<@Render(template = "Restaurant-stars-item.html") Integer> stars() {
			return IntStream.rangeClosed(1, 5).boxed().toList();
		}

		@Override
		public boolean evaluate(RenderEngine engine) {
			record A(Integer note, Object fill) {
			}
			return engine.match(A.class, (i, o) -> {
				o.setValue(i.note <= review.note() ? "currentColor" : "none");
			});
		}
	}

	public record User2(@Flatten User user, @Render(template = "image.html") Asset picture) {

		public static User2 of(User user, Persistence persistence) {
			if (user == null)
				return null;
			var p = persistence.getCrud(Asset.class).read(user.picture());
			return new User2(user, p);
		}
	}

	@Render(template = "Restaurant-RelatedRestaurants.html")
	public record RelatedRestaurants2(@Flatten Restaurant.RelatedRestaurants relatedRestaurants,
			List<Restaurant3> restaurants) {

		public static RelatedRestaurants2 of(Restaurant.RelatedRestaurants relatedRestaurants,
				Persistence persistence) {
			if (relatedRestaurants == null)
				return null;
			return new RelatedRestaurants2(relatedRestaurants,
					relatedRestaurants.restaurants().stream().map(x -> Restaurant3.of(x, persistence)).toList());
		}
	}

	@Render(template = "Restaurant-RelatedRestaurants-item.html")
	public record Restaurant3(@Flatten Restaurant restaurant, @Render(template = "image.html") Asset image) {

		public static Restaurant3 of(Long id, Persistence persistence) {
			if (id == null)
				return null;
			var r = persistence.getCrud(Restaurant.class).read(id);
			return new Restaurant3(r, persistence.getCrud(Asset.class).read(r.images().get(0)));
		}
	}
}
