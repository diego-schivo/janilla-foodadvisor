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

import com.janilla.frontend.RenderEngine;
import com.janilla.http.HttpExchange;
import com.janilla.media.HeaderField;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.web.TemplateHandlerFactory;

public class CustomTemplateHandlerFactory extends TemplateHandlerFactory {

	public Factory factory;

	public Persistence persistence;

	@Override
	protected void render(RenderEngine.Entry input, HttpExchange exchange) {
		var e = (CustomExchange) exchange;
		var a = exchange.getRequest().getHeaders().stream().filter(x -> x.name().equals("Accept"))
				.map(HeaderField::value).findFirst().orElse(null);
		if (e.layout == null && !a.equals("*/*")) {
			e.layout = Layout.of(e.locale, input, persistence);
			input = RenderEngine.Entry.of(null, e.layout, null);
		}
		super.render(input, exchange);
	}

	@Override
	protected RenderEngine createRenderEngine(HttpExchange exchange) {
		var e = (CustomRenderEngine) factory.create(RenderEngine.class);
		e.locale = ((CustomExchange) exchange).getLocale();
		return e;
	}
}
