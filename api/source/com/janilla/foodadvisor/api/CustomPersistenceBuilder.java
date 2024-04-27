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
import java.util.stream.Stream;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;
import com.janilla.util.Randomize;
import com.janilla.util.Util;

public class CustomPersistenceBuilder extends ApplicationPersistenceBuilder {

	static Pattern nonWord = Pattern.compile("[^\\p{L}]+", Pattern.UNICODE_CHARACTER_CLASS);

	static Set<String> safeWords = nonWord.splitAsStream(
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
					+ " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
					+ " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
					+ " Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
			.map(String::toLowerCase).sorted().collect(Collectors.toCollection(LinkedHashSet::new));

	static List<String> w = new ArrayList<>(safeWords);

	@Override
	public Persistence build() throws IOException {
		if (file == null) {
			Properties c;
			try {
				c = (Properties) Reflection.property(application.getClass(), "configuration").get(application);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
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
		if (!e) {
			{
				var u = new User();
				u.email = "admin@example.com";
				setHashAndSalt(u, "Password1!");
				p.getCrud(User.class).create(u);
			}

			for (var n : Randomize.elements(10, 15, w).distinct().toList()) {
				var o = Randomize.element(w);
				var u = new User();
				u.username = Util.capitalizeFirstChar(n) + " " + Util.capitalizeFirstChar(o);
				u.email = n + "@" + o + ".com";
				setHashAndSalt(u, n);

				var a = new Asset();
				var f = new File();
				f.name = Randomize.phrase(2, 4, () -> Randomize.element(w)).replace(' ', '-') + ".jpg";
				f.bytes = downloadImage("150x150", "avatar");
				p.getCrud(File.class).create(f);
				a.file = f.id;
				p.getCrud(Asset.class).create(a);
				u.picture = a.id;
				p.getCrud(User.class).create(u);
			}

			{
				var g = new Global();
				var n = new Global.Navigation();
				var l = new Link();
				l.uri = URI.create("/");
				l.text = Map.of(Locale.ENGLISH, "FoodAdvisor");
				n.leftButton = l;
				g.navigation = n;
				p.getCrud(Global.class).create(g);
			}

			{
				var q = new Page();
				q.slug = "";
				var h = new Hero();
				h.title = Map.of(Locale.ENGLISH, "Welcome to FoodAdvisor 🍕");
				var l = new Link();
				l.uri = URI.create("/restaurants");
				l.text = Map.of(Locale.ENGLISH, "Browse restaurants");
				h.buttons = List.of(l);
				q.components = List.of(h);
				p.getCrud(Page.class).create(q);
			}

			{
				var s = new Restaurants();
				s.slug = "restaurants";
				p.getCrud(Restaurants.class).create(s);
			}

			for (var n : Randomize.elements(5, 15, w).distinct().map(Util::capitalizeFirstChar).toList()) {
				var c = new Category();
				c.name = n;
				p.getCrud(Category.class).create(c);
			}

			for (var n : Randomize.elements(5, 15, w).distinct().map(Util::capitalizeFirstChar).toList()) {
				var q = new Place();
				q.name = n;
				p.getCrud(Place.class).create(q);
			}

			var r = ThreadLocalRandom.current();
			for (var i = r.nextInt(1, 2); i > 0; i--) {
				var s = new Restaurant();
				s.name = Util.capitalizeFirstChar(Randomize.phrase(2, 4, () -> Randomize.element(w)));
				s.slug = s.name.toLowerCase().replace(' ', '-');

				s.images = IntStream.range(0, r.nextInt(3, 7)).mapToLong(x -> {
					try {
						var a = new Asset();
						var f = new File();
						f.name = Randomize.phrase(2, 4, () -> Randomize.element(w)).replace(' ', '-') + ".jpg";
						f.bytes = downloadImage("1600x900", "food");
						p.getCrud(File.class).create(f);
						a.file = f.id;
						p.getCrud(Asset.class).create(a);
						return a.id;
					} catch (IOException f) {
						throw new UncheckedIOException(f);
					}
				}).boxed().toList();
				s.price = r.nextInt(1, 5);
				var d = Map.of(Locale.ENGLISH, Randomize.sentence(20, 30, () -> Randomize.element(w)));
				s.information = new Restaurant.Information(d, null, null);
				s.category = r.nextLong(1, p.getCrud(Category.class).count() + 1);
				s.place = r.nextLong(1, p.getCrud(Place.class).count() + 1);
				p.getCrud(Restaurant.class).create(s);
			}

			for (var i = r.nextInt(3, 6); i > 0; i--) {
				var s = new Review();
				s.creationTime = Randomize.instant(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.now());
				s.content = Randomize.sentence(20, 30, () -> Randomize.element(w));
				s.note = r.nextInt(1, 6);
				s.author = r.nextLong(2, p.getCrud(User.class).count() + 1);
				s.restaurant = r.nextLong(1, p.getCrud(Restaurant.class).count() + 1);
				p.getCrud(Review.class).create(s);
			}
		}
		return p;
	}

	@Override
	protected Stream<String> getPackageNames() {
		return Stream.concat(super.getPackageNames(), Stream.of("com.janilla.foodadvisor.api")).distinct();
	}

	static Random random = new SecureRandom();

	static void setHashAndSalt(User user, String password) {
		var p = password.toCharArray();
		var s = new byte[16];
		random.nextBytes(s);
		var h = hash(p, s);
		var f = HexFormat.of();
		user.passwordHash = f.formatHex(h);
		user.passwordSalt = f.formatHex(s);
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

	static byte[] downloadImage(String size, String term) throws IOException {
//		try (var c = new HttpClient()) {
//			c.setAddress(new InetSocketAddress("www.janilla.com", 443));
////			c.setAddress(new InetSocketAddress("source.unsplash.com", 443));
//			var k = Path.of(System.getProperty("user.home")).resolve("Downloads/jssesamples/samples/sslengine/testkeys");
//			var x = Net.getSSLContext(k, "passphrase".toCharArray());
//			c.setSSLContext(x);
//			
////			c.setAddress(new InetSocketAddress("source.unsplash.com", 443));
//			c.query(h -> {
////				try (var q = h.getRequest()) {
//				var q = h.getRequest();
//				q.setMethod(new HttpRequest.Method("GET"));
//				q.setURI(URI.create("/youtube_social_icon_red.png"));
////				q.setURI(URI.create("/random/?Food"));
//				q.getHeaders().add("Host", "www.janilla.com");
////				q.getHeaders().add("Host", "source.unsplash.com");
//				q.getHeaders().add("User-Agent", "curl/8.2.1");
//				q.getHeaders().add("Accept", "*/*");
//				q.close();
////				q.getHeaders().add("Connection", "close");
////				}
////				try (var s = h.getResponse()) {
//				var s = h.getResponse();
//				var t = s.getStatus();
//				System.out.println("t=" + t);
//				Channels.newInputStream((ReadableByteChannel) s.getBody()).readAllBytes();
////				}
//			});
//		}
		try (var s = URI.create("https://source.unsplash.com/" + size + "?" + term).toURL().openStream()) {
//		    Files.copy(in, Paths.get("C:/File/To/Save/To/image.jpg"));
			return s.readAllBytes();
		}
	}
}
