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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

import com.janilla.http.HttpRequest;
import com.janilla.json.Jwt;
import com.janilla.net.Net;
import com.janilla.persistence.Persistence;
import com.janilla.util.Util;
import com.janilla.web.Handle;
import com.janilla.web.Render;

public class AdminWeb {

	public Persistence persistence;

	public Properties configuration;

	@Handle(method = "GET", path = "/admin")
	public @Render("Admin.html") Object getPage() {
		return "page";
	}

	@Handle(method = "POST", path = "/admin/login")
	public String login(Credential credential) {
		var i = persistence.getCrud(User.class).find("email", credential.email);
		var u = i > 0 ? persistence.getCrud(User.class).read(i) : null;
		{
			var f = HexFormat.of();
			var h = f.formatHex(
					CustomPersistenceBuilder.hash(credential.password.toCharArray(), f.parseHex(u.passwordSalt())));
			if (!h.equals(u.passwordHash()))
				u = null;
		}
		var h = Map.of("alg", "HS256", "typ", "JWT");
		var p = Map.of("sub", u.email());
		var t = Jwt.generateToken(h, p, configuration.getProperty("foodadvisor.jwt.key"));
		return t;
	}

	@Handle(method = "POST", path = "/admin/upload")
	public long upload(HttpRequest request) throws IOException {
		byte[] bb;
		{
			var b = request.getHeaders().get("Content-Type").split(";")[1].trim().substring("boundary=".length());
			var c = (ReadableByteChannel) request.getBody();
			var cc = Channels.newInputStream(c).readAllBytes();
			var s = ("--" + b).getBytes();
			var ii = Util.findIndexes(cc, s);
			bb = IntStream.range(0, ii.length - 1)
					.mapToObj(i -> Arrays.copyOfRange(cc, ii[i] + s.length + 2, ii[i + 1] - 2)).findFirst()
					.orElse(null);
		}
		var i = Util.findIndexes(bb, "\r\n\r\n".getBytes(), 1)[0];
		var hh = Net.parseEntryList(new String(bb, 0, i), "\r\n", ":");
		var n = Arrays.stream(hh.get("Content-Disposition").split(";")).map(String::trim)
				.filter(x -> x.startsWith("filename=")).map(x -> x.substring(x.indexOf('=') + 1))
				.map(x -> x.startsWith("\"") && x.endsWith("\"") ? x.substring(1, x.length() - 1) : x).findFirst()
				.orElseThrow();
		bb = Arrays.copyOfRange(bb, i + 4, bb.length);
		return persistence.getCrud(File.class).create(new File(null, n, bb)).id();
	}

	public record Credential(String email, String password) {
	}
}
