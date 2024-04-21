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

import com.janilla.foodadvisor.api.Global;
import com.janilla.frontend.RenderEngine;
import com.janilla.http.HttpExchange;
import com.janilla.persistence.Persistence;
import com.janilla.web.TemplateHandlerFactory;

public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {

	protected static ThreadLocal<Layout> layout = new ThreadLocal<>();

	protected Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Override
	protected void render(RenderEngine.Entry input, HttpExchange exchange) throws IOException {
		var l = layout.get();
		var r = false;
		if (l == null) {
			var ii = persistence.getCrud(Global.class).list(0, 1).ids();
			var g = ii.length > 0 ? persistence.getCrud(Global.class).read(ii[0]) : null;
			l = new Layout(persistence, ((FoodAdvisorClientApp.Exchange) exchange).getLocale(), g, input);
			if (l != null) {
				input = RenderEngine.Entry.of(null, l, null);
				r = true;
			}
		}
		try {
			super.render(input, exchange);
		} finally {
			if (r)
				layout.remove();
		}
	}

	@Override
	protected RenderEngine newRenderEngine(HttpExchange exchange) {
		var e = new CustomRenderEngine();
		e.setLocale(((FoodAdvisorClientApp.Exchange) exchange).getLocale());
		return e;
	}
}
