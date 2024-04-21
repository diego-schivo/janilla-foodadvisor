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

class Content {

	selector;

	mode;

	typeReference;

	engine;

	type;

	headers = ['id', 'name'];

	rows;

	content;

	control;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			const t = this.typeReference.getType();
			if (t !== this.type) {
				this.type = t;
				delete this.content;
			}
			switch (this.type) {
				case 'Global':
					const s = await fetch(`/api/contents/${this.type}`, {
						headers: engine.admin.apiHeaders
					});
					const j = await s.json();
					this.content = j.length ? j[0] : { $type: this.type };
					break;
			}
			delete this.control;
			o.template = this.type != null ? 'Content' : 'Content-empty';
		}) || await engine.match(['list'], async (_, o) => {
			if (!this.content) {
				const s = await fetch(`/api/contents/${this.type}`, {
					headers: engine.admin.apiHeaders
				});
				this.rows = await s.json();
				o.template = 'Content-List';
			}
		}) || await engine.match(['headers', 'number'], async (_, o) => {
			o.template = 'Content-Header';
		}) || await engine.match(['createButton'], async (_, o) => {
			if (this.mode == null)
				o.template = 'Content-createButton';
		}) || await engine.match(['rows', 'number'], async (_, o) => {
			o.template = 'Content-Row';
		}) || await engine.match([undefined, 'cells'], async (i, o) => {
			o.value = this.headers.map(x => i[0][x]);
		}) || await engine.match(['cells', 'number'], async (_, o) => {
			o.template = 'Content-Cell'
		}) || await engine.match(['editButton'], async (_, o) => {
			if (this.mode == null)
				o.template = 'Content-editButton';
		}) || await engine.match(['selectButton'], async (_, o) => {
			if (this.mode === 'select')
				o.template = 'Content-selectButton';
		}) || await engine.match(['form'], async (_, o) => {
			if (this.content) {
				const c = new ObjectControl();
				c.selector = () => this.selector().querySelector('.form').firstElementChild;
				c.reference = {
					getValue: () => this.content,
					setValue: x => this.content = x
				};
				c.type = this.type;
				this.control = c;
				o.template = 'Content-Form';
			}
		});
	}

	listen = () => {
		this.selector().querySelector(':scope > .list .create')?.addEventListener('click', this.handleCreateClick);
		this.selector().querySelectorAll(':scope > .list .edit').forEach(x => x.addEventListener('click', this.handleEditClick));
		this.selector().querySelectorAll(':scope > .list .select').forEach(x => x.addEventListener('click', this.handleSelectClick));
		this.selector().querySelector(':scope > .form')?.addEventListener('submit', this.handleFormSubmit);
		this.selector().querySelector(':scope > .form .cancel')?.addEventListener('click', this.handleCancelClick);
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
		const i = parseInt(event.currentTarget.value, 10);
		const s = await fetch(`/api/contents/${this.type}/${i}`, {
			headers: this.engine.admin.apiHeaders
		});
		this.content = await s.json();
		await this.refresh();
	}

	handleSelectClick = async event => {
		const i = parseInt(event.currentTarget.value, 10);
		this.selector().dispatchEvent(new CustomEvent('contentselect', {
			bubbles: true,
			detail: { id: i }
		}));
	}

	handleFormSubmit = async event => {
		event.preventDefault();
		const f = event.currentTarget;
		const i = this.content.id;
		const r = i != null ? `/api/contents/${this.type}/${i}` : `/api/contents/${this.type}`;
		await fetch(r, {
			method: i != null ? 'PUT' : 'POST',
			headers: this.engine.admin.apiHeaders,
			body: new URLSearchParams(new FormData(f))
		});
		delete this.content;
		await this.refresh();
	}

	handleCancelClick = async event => {
		delete this.content;
		await this.refresh();
	}
}

export default Content;
