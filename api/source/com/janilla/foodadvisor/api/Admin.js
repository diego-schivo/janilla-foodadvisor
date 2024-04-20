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
import Login from './Login.js';
import RenderEngine from './RenderEngine.js';

class Admin {

	selector = () => document.body.firstElementChild;

	engine;

	types;

	collection;
	
	login;
	
	get apiHeaders() {
		const t = sessionStorage.getItem('jwtToken');
		return t ? { Authorization: `Bearer ${t}` } : {};
	}

	run = async () => {
		const e = new CustomRenderEngine();
		const h = await e.render({ value: this });
		document.body.innerHTML = h;
		this.listen();
	}

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			['collection', 'login'].forEach(x => delete this[x]);
			const s = await fetch('/api/collections', {
				headers: this.apiHeaders
			});
			if (s.ok) {
				this.types = await s.json();
				const c = new Collection();
				c.selector = () => this.selector().querySelector('.collection');
				this.collection = c;
				o.template = 'Admin-authenticated';
			} else if (s.status === 401) {
				const l = new Login();
				l.selector = () => this.selector().querySelector('.login');
				this.login = l;
				o.template = 'Admin-unauthenticated';
			}
		}) || await engine.match([this.types, 'number'], async (_, o) => {
			o.template = 'Admin-Option';
		});
	}

	listen = () => {
		const e = this.selector();
		e.addEventListener('login', this.handleLogin);
		e.querySelector('.type')?.addEventListener('change', this.handleTypeChange);
		this.collection?.listen();
		this.login?.listen();
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}
	
	handleLogin = async event => {
		sessionStorage.setItem('jwtToken', event.detail.token);
		await this.refresh();
	}

	handleTypeChange = event => {
		const s = event.currentTarget;
		this.collection.type = s.value;
		delete this.collection.content;
		this.collection.refresh();
	}
}

class CustomRenderEngine extends RenderEngine {

	get app() {
		return this.stack[0].value;
	}

	clone() {
		const e = new CustomRenderEngine();
		e.stack = [...this.stack];
		return e;
	}
}

export default Admin;
