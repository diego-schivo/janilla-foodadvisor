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

import java.util.List;
import java.util.Locale;

import com.janilla.foodadvisor.api.Global;
import com.janilla.foodadvisor.api.Link;
import com.janilla.web.Render;

@Render("Navbar.html")
public record Navbar(Global.Navigation navigation) {

	public @Render("Navbar-Logo.html") Link logo() {
		return navigation != null ? navigation.leftButton() : null;
	}

	public @Render("Navbar-Nav.html") List<Link> nav() {
		var ll = navigation != null ? navigation.links() : null;
		return ll != null && !ll.isEmpty() ? ll : null;
	}

	public @Render("Navbar-CTA.html") String cta() {
		return navigation != null && navigation.rightButton() != null ? "cta" : null;
	}

	public LocalSwitch localSwitch() {
		return new LocalSwitch(List.of(Locale.ENGLISH, Locale.FRANCE));
	}
}
