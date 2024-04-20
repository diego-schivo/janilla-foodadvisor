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

import com.janilla.reflect.Order;
import com.janilla.web.Render;

@Render(template = "Hero.html")
public class Hero implements Component { // , Renderer {

	@Order(1)
	private List<Long> images;

	@Order(2)
	private String title;

	@Order(3)
	private String text;

	@Order(4)
	private List<Link> buttons;

	public List<@Render(template = "Hero-Image.html") Long> getImages() {
		return images;
	}

	public void setImages(List<Long> images) {
		this.images = images;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Link> getButtons() {
		return buttons;
	}

	public void setButtons(List<Link> buttons) {
		this.buttons = buttons;
	}

//	@Override
//	public boolean evaluate(RenderEngine engine) {
//		record A(URI[] images, int index) {
//		}
//		return engine.match(A.class, (i, o) -> o.setTemplate("Hero-Image.html"));
//	}
}
