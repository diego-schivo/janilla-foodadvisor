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
import java.util.Locale;
import java.util.Map;

import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Order;
import com.janilla.reflect.Reflection;
import com.janilla.web.Render;

@Render(template = "Testimonial.html")
public class Testimonial implements Renderer {

	@Order(1)
	public Map<Locale, String> text;

	@Order(2)
	@Reference(User.class)
	public @Render(template = "Testimonial-author.html") Long author;

	@Override
	public boolean evaluate(RenderEngine engine) {
		record A(Testimonial testimonial, Long author) {
		}
		return engine.match(A.class, (i, o) -> {
			Persistence p;
			try {
				p = (Persistence) Reflection.property(engine.getClass(), "persistence").get(engine);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			User u;
			try {
				u = p.getCrud(User.class).read(i.author);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			o.setValue(u);
		});
	}
}
