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
import java.util.Properties;
import java.util.function.Supplier;

import com.janilla.http.HttpExchange;
import com.janilla.http.HttpRequest;
import com.janilla.http.HttpServer;
import com.janilla.io.IO;
import com.janilla.persistence.Persistence;
import com.janilla.reflect.Factory;
import com.janilla.util.Lazy;
import com.janilla.util.Util;

public class FoodAdvisorApiApp {

	public static void main(String[] args) throws IOException {
		var a = new FoodAdvisorApiApp();
		{
			var c = new Properties();
			try (var s = a.getClass().getResourceAsStream("configuration.properties")) {
				c.load(s);
			}
			a.configuration = c;
		}
		a.getPersistence();

		var s = a.new Server();
		s.setPort(Integer.parseInt(a.configuration.getProperty("foodadvisor.api.server.port")));
		s.setHandler(a.getHandler());
		s.run();
	}

	public Properties configuration;

	private Supplier<Persistence> persistence = Lazy.of(() -> new PersistenceBuilder().build());

	private Supplier<IO.Consumer<HttpExchange>> handler = Lazy.of(() -> new HandlerBuilder().build());

	public Persistence getPersistence() {
		return persistence.get();
	}

	public IO.Consumer<HttpExchange> getHandler() {
		return handler.get();
	}

	public class Exchange extends CustomExchange {
		{
			configuration = FoodAdvisorApiApp.this.configuration;
			persistence = getPersistence();
		}
	}

	public class HandlerBuilder extends CustomHandlerBuilder {
//		{
//			application = FoodAdvisorApiApp.this;
//		}
	}

	public class PersistenceBuilder extends CustomPersistenceBuilder {
		{
//			application = FoodAdvisorApiApp.this;
			factory = new Factory();
			factory.setTypes(Util.getPackageClasses(FoodAdvisorApiApp.class.getPackageName()).toList());
			factory.setEnclosing(FoodAdvisorApiApp.this);
		}
	}

	public class Server extends HttpServer {

		@Override
		protected HttpExchange newExchange(HttpRequest request) {
			return new Exchange();
		}
	}
}
