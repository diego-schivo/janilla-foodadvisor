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

import java.util.Locale;
import java.util.function.Supplier;

import com.janilla.http.HeaderField;
import com.janilla.http.Http;
import com.janilla.http.HttpCookie;
import com.janilla.http.HttpExchange;
import com.janilla.util.Lazy;

public class CustomExchange extends HttpExchange {

	public Layout layout;

	protected Locale locale;

	protected Supplier<Locale> cookieLocale = Lazy.of(() -> {
		var hh = getRequest().getHeaders();
		var h = hh != null
				? hh.stream().filter(x -> x.name().equals("Cookie")).map(HeaderField::value).findFirst().orElse(null)
				: null;
		var cc = h != null ? Http.parseCookieHeader(h) : null;
		var s = cc != null ? cc.get("lang") : null;
		return s != null ? Locale.forLanguageTag(s) : Locale.ENGLISH;
	});

	public Locale getLocale() {
		return locale != null ? locale : cookieLocale.get();
	}

	public void setLocale(Locale locale) {
		if (this.locale != null)
			throw new IllegalStateException();
		this.locale = locale;
		getResponse().getHeaders().add(new HeaderField("set-cookie",
				HttpCookie.of("lang", locale.toLanguageTag()).withPath("/").withSameSite("strict").format()));
	}
}
