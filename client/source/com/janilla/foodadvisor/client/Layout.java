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
import java.util.Locale;

import com.janilla.foodadvisor.api.Asset;
import com.janilla.foodadvisor.api.Global;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.Renderer;
import com.janilla.persistence.Persistence;
import com.janilla.web.Render;

@Render(template = "Layout.html")
public record Layout(Persistence persistence, Locale locale, Global global, RenderEngine.Entry entry)
		implements Renderer {

	public Navbar navbar() {
		return new Navbar(global != null ? global.navigation : null);
	}

	@Override
	public boolean evaluate(RenderEngine engine) {
		record A(Layout layout, Object content) {
		}
		record B(Long id, Object file) {
		}
//		record C(Testimonial testimonial, Long author) {
//		}
		return engine.match(A.class, (i, o) -> {
			o.setValue(entry.getValue());
			o.setType(entry.getType());
		}) || engine.match(B.class, (i, o) -> {
			Asset a;
			try {
				a = persistence.getCrud(Asset.class).read(i.id);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			o.setValue(a.file);
//		}) || engine.match(C.class, (i, o) -> {
//			User u;
//			try {
//				u = persistence.getCrud(User.class).read(i.author);
//			} catch (IOException e) {
//				throw new UncheckedIOException(e);
//			}
//			o.setValue(u);
		});
	}
}
