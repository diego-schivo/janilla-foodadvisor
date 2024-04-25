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
import java.util.Locale;
import java.util.Map;

import com.janilla.persistence.Index;
import com.janilla.persistence.Store;
import com.janilla.reflect.Order;
import com.janilla.web.Render;

@Store
public class Restaurant {

	@Order(1)
	public Long id;

	@Order(2)
	public String name;

	@Index
	@Order(3)
	public String slug;

	@Order(4)
	@Reference(Asset.class)
	public List<@Render(template = "image.html") Long> images;
	
	@Order(5)
	public Integer price;

	@Order(6)
	public Map<Locale, String> description;

	@Index
	@Order(7)
	@Reference(Category.class)
	public Long category;

	@Index
	@Order(8)
	@Reference(Place.class)
	public Long place;
}
