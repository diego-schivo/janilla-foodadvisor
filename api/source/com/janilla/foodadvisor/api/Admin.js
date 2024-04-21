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
import Content from './Content.js';
import Login from './Login.js';
import RenderEngine from './RenderEngine.js';

class Admin {

	selector = () => document.body.firstElementChild;

	engine;

	contentTypes;

	availableLocales;

	login;

	content;

	contentType;

	locales;

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
			['contentTypes', 'availableLocales', 'login', 'content', 'contentType', 'locales'].forEach(x => delete this[x]);
			const s = await fetch('/api/content-types', {
				headers: this.apiHeaders
			});
			if (s.status === 401) {
				const l = new Login();
				l.selector = () => this.selector().querySelector('.login');
				this.login = l;
				o.template = 'Admin-unauthenticated';
			} else if (s.ok) {
				this.contentTypes = await s.json();
				this.availableLocales = ['en', 'fr-FR'];
				const c = new Content();
				c.selector = () => this.selector().querySelector('.content');
				c.typeReference = { getType: () => this.contentType };
				this.content = c;
				o.template = 'Admin-authenticated';
			}
		}) || (this.contentTypes && await engine.match([this.contentTypes, 'number'], async (_, o) => {
			o.template = 'Admin-contentType';
		})) || (this.availableLocales && await engine.match([this.availableLocales, 'number'], async (_, o) => {
			o.template = 'Admin-availableLocale';
		}));
	}

	listen = () => {
		const e = this.selector();
		e.addEventListener('login', this.handleLogin);
		e.querySelector(':scope > .header [name="contentType"]')?.addEventListener('change', this.handleContentTypeChange);
		e.querySelectorAll(':scope > .header [name="locale"]').forEach(x => x.addEventListener('change', this.handleLocaleChange));
		this.login?.listen();
		this.content?.listen();
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleLogin = async event => {
		sessionStorage.setItem('jwtToken', event.detail.token);
		await this.refresh();
	}

	handleContentTypeChange = event => {
		this.contentType = event.currentTarget.value;
		this.content.refresh();
	}

	handleLocaleChange = () => {
		this.locales = [...this.selector().querySelectorAll(':scope > .header [name="locale"]:checked')].map(x => x.value);
		this.content.refresh();
	}
}

class CustomRenderEngine extends RenderEngine {

	get admin() {
		return this.stack[0].value;
	}

	get controls() {
		return this.stack.reduce((cc, e) => {
			if (e.value?.constructor?.name.endsWith('Control'))
				cc.push(e.value);
			return cc;
		}, []);
	}

	clone() {
		const e = new CustomRenderEngine();
		e.stack = [...this.stack];
		return e;
	}
}

export default Admin;
