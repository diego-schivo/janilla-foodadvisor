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
import java.util.List;

import com.janilla.persistence.Persistence;
import com.janilla.persistence.Store;
import com.janilla.reflect.Reflection;
import com.janilla.web.Handle;

public class ContentWeb {

	Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/api/content-types")
	public List<String> listTypes() {
		return TypeWeb.classes.get().stream().filter(x -> x.isAnnotationPresent(Store.class)).map(Class::getSimpleName)
				.sorted().toList();
	}

	@Handle(method = "GET", path = "/api/contents/(\\w+)")
	public <T> List<T> list(String name) throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		var c = (Class<T>) Class.forName("com.janilla.foodadvisor.api." + name);
		var ii = persistence.getCrud(c).list();
		return persistence.getCrud(c).read(ii).toList();
	}

	@Handle(method = "POST", path = "/api/contents/(\\w+)")
	public <T> void create(String name, T object) throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		var c = (Class<T>) Class.forName("com.janilla.foodadvisor.api." + name);
		persistence.getCrud(c).create(object);
	}

	@Handle(method = "GET", path = "/api/contents/(\\w+)/(\\d+)")
	public <T> T read(String name, long id) throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		var c = (Class<T>) Class.forName("com.janilla.foodadvisor.api." + name);
		return persistence.getCrud(c).read(id);
	}

	@Handle(method = "PUT", path = "/api/contents/(\\w+)/(\\d+)")
	public <T> void update(String name, long id, T object) throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		var c = (Class<T>) Class.forName("com.janilla.foodadvisor.api." + name);
		persistence.getCrud(c).update(id, t -> Reflection.copy(object, t, n -> !n.equals("id")));
	}

	@Handle(method = "DELETE", path = "/api/contents/(\\w+)/(\\d+)")
	public <T> void delete(String name, long id) throws ClassNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		var c = (Class<T>) Class.forName("com.janilla.foodadvisor.api." + name);
		persistence.getCrud(c).delete(id);
	}
}
