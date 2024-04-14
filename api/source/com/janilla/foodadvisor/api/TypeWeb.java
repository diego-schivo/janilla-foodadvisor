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

import java.util.List;
import java.util.function.Supplier;

import com.janilla.reflect.Reflection;
import com.janilla.util.Lazy;
import com.janilla.util.Util;
import com.janilla.web.Handle;

public class TypeWeb {

	static Supplier<List<Class<?>>> classes = Lazy
			.of(() -> Util.getPackageClasses("com.janilla.foodadvisor.core").toList());

	@Handle(method = "GET", path = "/api/types/(\\w+)/properties")
	public List<String> getTypeProperties(String name) throws ClassNotFoundException {
		var c = Class.forName("com.janilla.foodadvisor.core." + name);
		var pp = Reflection.properties(c);
		return pp.toList();
	}
}
