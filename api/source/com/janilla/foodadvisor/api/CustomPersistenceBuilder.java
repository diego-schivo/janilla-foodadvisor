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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Stream;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.janilla.persistence.ApplicationPersistenceBuilder;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Reflection;

public class CustomPersistenceBuilder extends ApplicationPersistenceBuilder {

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
			var u = new User();
			u.email = "admin@example.com";
			setHashAndSalt(u, "Password1!");
			p.getCrud(User.class).create(u);
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
		user.hash = f.formatHex(h);
		user.salt = f.formatHex(s);
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
}
