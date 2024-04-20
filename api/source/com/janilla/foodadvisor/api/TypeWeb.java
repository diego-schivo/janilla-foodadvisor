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

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.janilla.reflect.Reflection;
import com.janilla.util.Lazy;
import com.janilla.util.Util;
import com.janilla.web.Handle;

public class TypeWeb {

	static Supplier<List<Class<?>>> classes = Lazy
			.of(() -> Util.getPackageClasses("com.janilla.foodadvisor.api").toList());

	@Handle(method = "GET", path = "/api/types/([\\w.]+)/properties")
	public List<Property> getTypeProperties(String name) throws ClassNotFoundException {
		var c = Class.forName("com.janilla.foodadvisor.api." + name.replace('.', '$'));
		var pp = Reflection.properties(c);
		return pp.map(n -> {
			var g = Reflection.property(c, n);
			Class<?> t;
			Class<?>[] aa;
			switch (g.getGenericType()) {
			case ParameterizedType x:
				t = (Class<?>) x.getRawType();
				aa = Arrays.stream(x.getActualTypeArguments()).map(y -> (Class<?>) y).toArray(Class<?>[]::new);
				break;
			case Class<?> x:
				t = x;
				aa = null;
				break;
			default:
				throw new RuntimeException();
			}
			return new Property(n, foo(t)[0],
					aa != null ? Arrays.stream(aa).map(TypeWeb::foo).toArray(String[][]::new) : null);
		}).toList();
	}

	static String[] foo(Class<?> type) {
		if (type == null)
			return null;
//		var n = type.getSimpleName();
		var n = type.getName();
		if (!type.isPrimitive()) {
			n = n.substring(type.getPackageName().length() + 1).replace('$', '.');
			if (type.getPackageName().startsWith("java."))
				n = n.toLowerCase();
		}
		return n.equals("Component") ? new String[] { "Hero", "Features" } : new String[] { n };
	}

	public record Property(String name, String type, String[][] typeArguments) {
	}
}
