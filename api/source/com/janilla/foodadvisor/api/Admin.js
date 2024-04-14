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
import Collection from './Collection.js';
import RenderEngine from './RenderEngine.js';

class Admin {

	selector = () => document.body.firstElementChild;

	collection;

	run = async () => {
		const e = new RenderEngine();
		const h = await e.render({ value: this });
		document.body.innerHTML = h;
		this.listen();
	}

	render = async engine => {
		return await engine.match([this], async (_, o) => o.template = 'Admin-Body')
			|| await engine.match([this, 'collections'], async (_, o) => {
				const s = await fetch('/api/collections');
				o.value = await s.json();
			}) || await engine.match([this, 'collections', 'number'], async (_, o) => o.template = 'Admin-Collection')
			|| await engine.match([this, 'collection'], async (_, o) => {
				const c = new Collection();
				c.selector = () => this.selector().children[1];
				o.value = this.collection = c;
			});
	}

	listen = () => {
		this.selector().firstElementChild.addEventListener('click', this.handleClick);
	}

	handleClick = event => {
		const a = event.target.closest('a');
		if (!a)
			return;
		event.preventDefault();
		this.collection.name = a.textContent;
		this.collection.refresh();
	}
}

export default Admin;
