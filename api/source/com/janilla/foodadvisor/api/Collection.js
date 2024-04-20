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
import ObjectControl from './ObjectControl.js';

class Collection {

	selector;

	type;

	engine;

	headers = ['id', 'name'];

	rows;

	content;

	control;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			switch (this.type) {
				case 'Global':
					const s = await fetch(`/api/collections/${this.type}`, {
						headers: engine.app.apiHeaders
					});
					const j = await s.json();
					this.content = j.length ? j[0] : { $type: this.type };
					break;
			}
			delete this.control;
			o.template = this.type != null ? 'Collection' : 'Collection-empty';
		}) || await engine.match(['list'], async (_, o) => {
			if (!this.content) {
				const s = await fetch(`/api/collections/${this.type}`, {
					headers: engine.app.apiHeaders
				});
				this.rows = await s.json();
				o.template = 'Collection-List';
			}
		}) || await engine.match(['headers', 'number'], async (_, o) => {
			o.template = 'Collection-Header';
		}) || await engine.match(['rows', 'number'], async (_, o) => {
			o.template = 'Collection-Row';
		}) || await engine.match([undefined, 'cells'], async (i, o) => {
			o.value = this.headers.map(x => i[0][x]);
		}) || await engine.match(['cells', 'number'], async (_, o) => {
			o.template = 'Collection-Cell'
		}) || await engine.match(['form'], async (_, o) => {
			if (this.content) {
				const c = new ObjectControl();
				c.selector = () => this.selector().querySelector('.form').firstElementChild;
				c.binding = {
					getter: () => this.content,
					setter: x => this.content = x
				};
				c.type = this.type;
				this.control = c;
				o.template = 'Collection-Form';
			}
		});
	}

	listen = () => {
		this.selector().querySelector('.create:not([name])')?.addEventListener('click', this.handleCreateClick);
		this.selector().querySelectorAll('.edit').forEach(x => x.addEventListener('click', this.handleEditClick));
		this.selector().querySelector('.save')?.addEventListener('click', this.handleSaveClick);
		this.selector().querySelector('.cancel')?.addEventListener('click', this.handleCancelClick);
		this.control?.listen();
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async () => {
		this.content = { $type: this.type };
		await this.refresh();
	}

	handleEditClick = async event => {
		const b = event.currentTarget;
		const i = parseInt(b.value, 10);
		const s = await fetch(`/api/collections/${this.type}/${i}`, {
			headers: this.engine.app.apiHeaders
		});
		this.content = await s.json();
		await this.refresh();
	}

	handleSaveClick = async event => {
		const b = event.currentTarget;
		event.preventDefault();
		const i = this.content.id;
		const r = i != null ? `/api/collections/${this.type}/${i}` : `/api/collections/${this.type}`;
		await fetch(r, {
			method: i != null ? 'PUT' : 'POST',
			headers: this.engine.app.apiHeaders,
			body: new URLSearchParams(new FormData(b.form, b))
		});
		delete this.content;
		await this.refresh();
	}

	handleCancelClick = async event => {
		event.preventDefault();
		delete this.content;
		await this.refresh();
	}
}

export default Collection;
