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
package com.janilla.foodadvisor.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.util.Randomize;
import com.janilla.util.Util;

public abstract class CustomPersistenceBuilder extends ApplicationPersistenceBuilder {

	@Override
	public Persistence build() {
		if (file == null) {
			var a = factory.getEnclosing();
			var c = (Properties) Reflection.property(a.getClass(), "configuration").get(a);
			var p = c.getProperty("foodadvisor.database.file");
			if (p.startsWith("~"))
				p = System.getProperty("user.home") + p.substring(1);
			file = Path.of(p);
		}
		var e = Files.exists(file);
		var p = super.build();
		p.setTypeResolver(x -> {
			try {
				return Class.forName("com.janilla.foodadvisor.api." + x.replace('.', '$'));
			} catch (ClassNotFoundException f) {
				throw new RuntimeException(f);
			}
		});
		if (!e)
			seed(p);
		return p;
	}

//	@Override
//	protected Stream<String> getPackageNames() {
//		return Stream.concat(super.getPackageNames(), Stream.of("com.janilla.foodadvisor.api")).distinct();
//	}

	static Random random = new SecureRandom();

	static User setHashAndSalt(User user, String password) {
		var p = password.toCharArray();
		var s = new byte[16];
		random.nextBytes(s);
		var h = hash(p, s);
		var f = HexFormat.of();
		return new User(user.id(), user.username(), user.email(), f.formatHex(h), f.formatHex(s), user.picture(),
				user.job());
	}

	static byte[] hash(char[] password, byte[] salt) {
		var s = new PBEKeySpec(password, salt, 10000, 512);
		try {
			var f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			return f.generateSecret(s).getEncoded();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	static Pattern nonWord = Pattern.compile("[^\\p{L}]+", Pattern.UNICODE_CHARACTER_CLASS);

	static Set<String> safeWords = nonWord.splitAsStream(
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
					+ " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
					+ " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
					+ " Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
			.map(String::toLowerCase).sorted().collect(Collectors.toCollection(LinkedHashSet::new));

	static List<String> w = new ArrayList<>(safeWords);

	static void seed(Persistence persistence) {
		var r = ThreadLocalRandom.current();

		persistence.getCrud(User.class).create(
				setHashAndSalt(new User(null, null, "admin@example.com", null, null, null, null), "Password1!"));

//		for (var n : Randomize.elements(10, 15, w).distinct().toList()) {
		for (var n : Randomize.elements(3, 4, w).distinct().toList()) {
			var o = Randomize.element(w);

			var p = Randomize.phrase(2, 4, () -> Randomize.element(w)).replace(' ', '-') + ".jpg";
			var bb = getRandomImage("150x150", "avatar");
			var f = persistence.getCrud(File.class).create(new File(null, p, bb));
			var a = persistence.getCrud(Asset.class).create(new Asset(null, f.id()));

			var u = setHashAndSalt(new User(null, Util.capitalizeFirstChar(n) + " " + Util.capitalizeFirstChar(o),
					n + "@" + o + ".com", null, null, a.id(), null), n);
			persistence.getCrud(User.class).create(u);
		}

		{
			var n = new Global.Navigation(
					List.of(new Link(URI.create("/restaurants"), Map.of(Locale.ENGLISH, "Restaurants"))),
					new Link(URI.create("/"), Map.of(Locale.ENGLISH, "FoodAdvisor")),
					new Link(URI.create("https://janilla.com"), Map.of(Locale.ENGLISH, "Get Started with Janilla")));
			var f = new Global.Footer(Map.of(Locale.ENGLISH, "Made with Janilla"));
			persistence.getCrud(Global.class).create(new Global(null, n, f));
		}

		{
			var ii = IntStream.range(0, 4).mapToLong(x -> {
				var n = Randomize.phrase(2, 4, () -> Randomize.element(w)).replace(' ', '-') + ".jpg";
				var bb = getRandomImage(x > 0 && x < 3 ? "300x450" : "450x300", "food");
				var f = persistence.getCrud(File.class).create(new File(null, n, bb));
				var a = persistence.getCrud(Asset.class).create(new Asset(null, f.id()));
				return a.id();
			}).boxed().toList();
			var t = Map.of(Locale.ENGLISH,
					"Welcome to FoodAdvisor " + String.valueOf(Character.toChars(r.nextInt(0x1F345, 0x1F380))));
			var u = Map.of(Locale.ENGLISH,
					"Janilla FoodAdvisor is a clone of the Strapi demo app listing restaurants around the world.");
			var bb = List.of(new Link(URI.create("/restaurants"), Map.of(Locale.ENGLISH, "Browse restaurants")));
			var h = new Hero(ii, t, u, bb);
			persistence.getCrud(Page.class).create(new Page(null, "Homepage", "", List.of(h)));
		}

		{
			var h = new Header(
					Map.of(Locale.ENGLISH,
							Util.capitalizeFirstChar(Randomize.phrase(3, 6, () -> Randomize.element(w)))),
					Map.of(Locale.ENGLISH,
							Util.capitalizeFirstChar(Randomize.phrase(2, 4, () -> Randomize.element(w)))));
			persistence.getCrud(Restaurants.class).create(new Restaurants(null, "restaurants", h));
		}

		for (var n : Randomize.elements(5, 15, w).distinct().map(Util::capitalizeFirstChar).toList())
			persistence.getCrud(Category.class).create(new Category(null, n));

		for (var n : Randomize.elements(5, 15, w).distinct().map(Util::capitalizeFirstChar).toList())
			persistence.getCrud(Place.class).create(new Place(null, n));

//		for (var i = r.nextInt(13, 25); i > 0; i--) {
		for (var i = r.nextInt(3, 4); i > 0; i--) {
			var n1 = Util.capitalizeFirstChar(Randomize.phrase(2, 4, () -> Randomize.element(w)));
			var s1 = n1.toLowerCase().replace(' ', '-');

			var ii1 = IntStream.range(0, r.nextInt(3, 7)).mapToLong(x -> {
				var n = Randomize.phrase(2, 4, () -> Randomize.element(w)).replace(' ', '-') + ".jpg";
				var bb = getRandomImage(r.nextBoolean() ? "450x675" : "675x450", "food");
				var f = persistence.getCrud(File.class).create(new File(null, n, bb));
				var a = persistence.getCrud(Asset.class).create(new Asset(null, f.id()));
				return a.id();
			}).boxed().toList();
			var c1 = r.nextLong(1, persistence.getCrud(Category.class).count() + 1);
			var p1 = r.nextLong(1, persistence.getCrud(Place.class).count() + 1);
			var q1 = r.nextInt(1, 5);
			var d = Map.of(Locale.ENGLISH, Randomize.sentence(20, 30, () -> Randomize.element(w)));
			var hh = IntStream.range(0, r.nextInt(1, 4)).mapToObj(
					x -> new Restaurant.OpeningHour(Randomize.element(w), Randomize.element(w), Randomize.element(w)))
					.toList();
			var l = new Restaurant.Location(Randomize.element(w), Randomize.element(w), Randomize.element(w));
			var i1 = new Restaurant.Information(d, hh, l);
			Restaurant.RelatedRestaurants rr1;
			{
				var c = persistence.getCrud(Restaurant.class).count();
				if (c > 0) {
					var h = new Header(
							Map.of(Locale.ENGLISH,
									Util.capitalizeFirstChar(Randomize.phrase(3, 6, () -> Randomize.element(w)))),
							Map.of(Locale.ENGLISH,
									Util.capitalizeFirstChar(Randomize.phrase(2, 4, () -> Randomize.element(w)))));
					var rr = r.longs(r.nextLong(1, 4), 1, c + 1).distinct().boxed().toList();
					rr1 = new Restaurant.RelatedRestaurants(h, rr);
				} else
					rr1 = null;
			}
			var s = persistence.getCrud(Restaurant.class)
					.create(new Restaurant(null, n1, s1, ii1, c1, p1, q1, i1, rr1));

			for (var j = r.nextInt(6); j > 0; j--) {
				var t = Randomize.instant(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.now());
				var c = Randomize.sentence(20, 30, () -> Randomize.element(w));
				var n = r.nextInt(1, 6);
				var a = r.nextLong(2, persistence.getCrud(User.class).count() + 1);
				persistence.getCrud(Review.class).create(new Review(null, t, c, n, a, s.id()));
			}
		}
	}

	static byte[] getRandomImage(String size, String term) {
		try (var s = URI.create("https://source.unsplash.com/" + size + "?" + term).toURL().openStream()) {
			return s.readAllBytes();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
