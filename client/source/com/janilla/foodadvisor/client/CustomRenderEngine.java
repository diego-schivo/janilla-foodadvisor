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

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Locale;
import java.util.Map;

import com.janilla.frontend.RenderEngine;

public class CustomRenderEngine extends RenderEngine {

	protected Locale locale;

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	protected Entry entryOf(Object key, Object value, AnnotatedType type) {
		if (value instanceof Map<?, ?> m && type instanceof AnnotatedParameterizedType t) {
			var aa = t.getAnnotatedActualTypeArguments();
			if (aa.length == 2 && aa[0].getType() == Locale.class) {
				var v = m.containsKey(locale) ? m.get(locale)
						: !locale.equals(Locale.ENGLISH) ? m.get(Locale.ENGLISH) : null;
				return super.entryOf(key, v, aa[1]);
			}
		}
		return super.entryOf(key, value, type);
	}
}
