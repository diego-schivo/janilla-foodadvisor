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

import com.janilla.foodadvisor.api.Global;
import com.janilla.foodadvisor.api.Link;
import com.janilla.frontend.RenderEngine;
import com.janilla.frontend.RenderParticipant;
import com.janilla.persistence.Persistence;
import com.janilla.web.Render;

@Render("Layout.html")
public record Layout(Locale locale, Global global, RenderEngine.Entry entry) implements RenderParticipant {

	public static Layout of(Locale locale, RenderEngine.Entry entry, Persistence persistence) {
		var ii = persistence.crud(Global.class).list(0, 1).ids();
		var g = ii.length > 0 ? persistence.crud(Global.class).read(ii[0]) : null;
		return new Layout(locale, g, entry);
	}

	public Navbar navbar() {
		return global != null ? new Navbar(global.navigation()) : null;
	}

	@Override
	public boolean render(RenderEngine engine) {
		record A(Layout layout, Object content) {
		}
		record B(Link link, Object target) {
		}
		return engine.match(A.class, (i, o) -> {
			o.setValue(entry.getValue());
			o.setType(entry.getType());
		}) || engine.match(B.class, (i, o) -> {
			o.setValue(i.link.uri().getScheme() != null ? "target=\"_blank\"" : null);
		});
	}
}
