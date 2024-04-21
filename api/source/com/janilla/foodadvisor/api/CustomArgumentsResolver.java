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

import java.lang.reflect.Type;
import java.util.function.Supplier;

import com.janilla.http.HttpExchange;
import com.janilla.util.EntryList;
import com.janilla.web.MethodArgumentsResolver;

public class CustomArgumentsResolver extends MethodArgumentsResolver {

	@Override
	protected Object resolveArgument(Type type, HttpExchange exchange, Supplier<String[]> values,
			EntryList<String, String> entries, com.janilla.io.IO.Supplier<String> body) {
		var q = exchange.getRequest();
		if (type == Object.class && q.getURI().getPath().startsWith("/api/contents/"))
			try {
				type = Class.forName("com.janilla.foodadvisor.api."
						+ q.getURI().getPath().substring("/api/contents/".length()).split("/")[0].replace('.', '$'));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		return super.resolveArgument(type, exchange, values, entries, body);
	}
}
