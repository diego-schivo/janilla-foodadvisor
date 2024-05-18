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
import java.util.Properties;
import java.util.function.Supplier;

import com.janilla.foodadvisor.api.CustomPersistenceBuilder;
import com.janilla.foodadvisor.api.FoodAdvisorApiApp;
import com.janilla.http.HttpExchange;
import com.janilla.http.HttpRequest;
import com.janilla.http.HttpServer;
import com.janilla.io.IO;
import com.janilla.reflect.Factory;
import com.janilla.util.Lazy;
import com.janilla.util.Util;

public class FoodAdvisorClientApp {

	public static void main(String[] args) throws IOException {
		var a = new FoodAdvisorClientApp();
		{
			var c = new Properties();
			try (var s = a.getClass().getResourceAsStream("configuration.properties")) {
				c.load(s);
			}
			a.setConfiguration(c);
		}
		a.getPersistence();

		var s = a.new Server();
		s.setPort(Integer.parseInt(a.getConfiguration().getProperty("foodadvisor.client.server.port")));
		s.setHandler(a.getHandler());
		s.run();
	}

	private Properties configuration;

	private Supplier<Persistence> persistence = Lazy.of(() -> (Persistence) new PersistenceBuilder().build());

	private Supplier<IO.Consumer<HttpExchange>> handler = Lazy.of(() -> new HandlerBuilder().build());

	public Properties getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	public Persistence getPersistence() {
		return persistence.get();
	}

	public IO.Consumer<HttpExchange> getHandler() {
		return handler.get();
	}

	public class Exchange extends CustomExchange {
	}

	public class HandlerBuilder extends CustomHandlerBuilder {
//		{
//			application = FoodAdvisorClientApp.this;
//		}
	}

	public class Persistence extends CustomPersistence {
	}

	public class PersistenceBuilder extends CustomPersistenceBuilder {
		{
//			application = FoodAdvisorClientApp.this;
			factory = new Factory();
			factory.setTypes(Util.getPackageClasses(FoodAdvisorApiApp.class.getPackageName()).toList());
			factory.setEnclosing(FoodAdvisorClientApp.this);
		}
	}

	public class RenderEngine extends CustomRenderEngine {
		{
			persistence = FoodAdvisorClientApp.this.getPersistence();
		}
	}

	public class Server extends HttpServer {

		@Override
		protected HttpExchange createExchange(HttpRequest request) {
			return new Exchange();
		}
	}

	public class TemplateHandlerFactory extends CustomTemplateHandlerFactory {
		{
			application = FoodAdvisorClientApp.this;
		}
	}
}
