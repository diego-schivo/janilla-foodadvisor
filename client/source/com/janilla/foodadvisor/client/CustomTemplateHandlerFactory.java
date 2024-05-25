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

import java.util.function.Consumer;

import com.janilla.foodadvisor.api.Global;
import com.janilla.frontend.RenderEngine;
import com.janilla.http.HttpExchange;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.web.TemplateHandlerFactory;

public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {

	public Factory factory;

	public Persistence persistence;

	@Override
	protected void render(RenderEngine.Entry input, HttpExchange exchange) {
		applyLayout(input, exchange, x -> super.render(x, exchange));
	}

	@Override
	protected RenderEngine createRenderEngine(HttpExchange exchange) {
		var e = (CustomRenderEngine) factory.create(RenderEngine.class);
		e.locale = ((CustomExchange) exchange).getLocale();
		return e;
	}

	static ThreadLocal<Layout> layout = new ThreadLocal<>();

	void applyLayout(RenderEngine.Entry input, HttpExchange exchange, Consumer<RenderEngine.Entry> consumer) {
		var l = layout.get();
		var n = l == null;
		if (n) {
			var m = ((CustomExchange) exchange).getLocale();
			var ii = persistence.crud(Global.class).list(0, 1).ids();
			var g = ii.length > 0 ? persistence.crud(Global.class).read(ii[0]) : null;
			l = new Layout(m, g, input);
			layout.set(l);
			input = RenderEngine.Entry.of(null, l, null);
		}
		try {
			consumer.accept(input);
		} finally {
			if (n)
				layout.remove();
		}
	}
}
