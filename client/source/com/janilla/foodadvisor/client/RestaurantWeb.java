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
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.janilla.foodadvisor.api.Asset;
import com.janilla.foodadvisor.api.Category;
import com.janilla.foodadvisor.api.Restaurant;
import com.janilla.foodadvisor.api.Review;
import com.janilla.foodadvisor.api.User;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.reflect.Flatten;
import com.janilla.web.Handle;
import com.janilla.web.Render;

public class RestaurantWeb {

	private static DecimalFormat noteFormatter = new DecimalFormat("0.#");

	CustomPersistence persistence;

	public void setPersistence(CustomPersistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/restaurants/([a-z-]+)")
	public Restaurant2 getRestaurant(String slug) throws IOException {
		var i = persistence.getCrud(Restaurant.class).find("slug", slug);
		var r = persistence.getCrud(Restaurant.class).read(i);
		var cc = persistence.getCategories();
		List<Review> rr;
		{
			var ii = persistence.getCrud(Review.class).filter("restaurant", r.id);
			rr = persistence.getCrud(Review.class).read(ii).toList();
		}
		var g = new Gallery(r.images.stream().map(x -> {
			try {
				return persistence.getCrud(Asset.class).read(x);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).toList());
		var c = cc.get(r.category);
		var s = rr.size() == 1 ? "1 Review" : rr.size() + " Reviews";
		var p = IntStream.range(0, r.price).mapToObj(x -> "€").collect(Collectors.joining(" "));
		var a = rr.stream().mapToInt(x -> x.note).average().orElse(0);
		var qq = rr.stream().map(x -> {
			try {
				var u = persistence.getCrud(User.class).read(x.author);
				var d = Duration.between(x.creationTime, Instant.now());
				var l = d.toDaysPart();
				String t;
				if (l > 0)
					t = l + " days ago";
				else {
					l = d.toHoursPart();
					if (l > 0)
						t = l + " hours ago";
					else {
						l = d.toMinutesPart();
						if (l > 0)
							t = l + " minutes ago";
						else {
							l = d.toSecondsPart();
							t = l + " seconds ago";
						}
					}
				}
				var q = persistence.getCrud(Asset.class).read(u.picture);
				return new Review2(x, new User2(u, q), t);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).toList();
		List<Map.Entry<String, Integer>> nn;
		{
			var kk = new String[] { "Excellent", "Good", "Average", "Bellow Average", "Poor" };
			var vv = new int[5];
			for (var x : rr)
				vv[5 - x.note]++;

			nn = IntStream.range(0, kk.length).mapToObj(x -> Map.entry(kk[x], vv[x])).toList();
		}
		return new Restaurant2(r, g, c, s, p, a, nn, qq);
	}

	@Render(template = "Restaurant.html")
	public record Restaurant2(@Flatten Restaurant restaurant, Gallery gallery, Category category, String reviewSummary,
			String price, double average,
			List<Map.@Render(template = "Restaurant-note.html") Entry<String, Integer>> notes,
			@Render(template = "Restaurant-reviews.html") List<Review2> reviews) implements Renderer {

		public String className() {
			return "restaurant";
		}

		public List<@Render(template = "Restaurant-star.html") Integer> stars() {
			return IntStream.rangeClosed(1, 5).boxed().toList();
		}

		public String averageSummary() {
			return noteFormatter.format(average) + "/5";
		}

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
	public record Review2(@Flatten Review review, User2 author, String timeSummary) implements Renderer {

		public List<@Render(template = "Restaurant-star.html") Integer> stars() {
			return IntStream.rangeClosed(1, 5).boxed().toList();
		}

		@Override
		public boolean evaluate(RenderEngine engine) {
			record A(Integer note, Object fill) {
			}
			return engine.match(A.class, (i, o) -> {
				o.setValue(i.note <= review.note ? "currentColor" : "none");
			});
		}
	}

	public record User2(@Flatten User user, @Render(template = "image.html") Asset picture) {
	}
}
