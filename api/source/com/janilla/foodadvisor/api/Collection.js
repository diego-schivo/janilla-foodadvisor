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
import ComponentList from './ComponentList.js';

class Collection {

	selector;

	engine;

	name;

	properties;

	headers;

	rows;

	id;

	content;

	componentList;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			if (this.name == null)
				o.template = 'Collection-empty';
			else {
				const s = await fetch(`/api/types/${this.name}/properties`);
				this.properties = await s.json();
				this.headers = this.properties.filter(x => ['id', 'name'].includes(x));
				o.template = 'Collection';
			}
		}) || await engine.match(['list'], async (_, o) => {
			if (!this.content) {
				const s = await fetch(`/api/collections/${this.name}`);
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
			if (this.content)
				o.template = 'Collection-Form';
		}) || await engine.match(['fields'], async (_, o) => {
			o.value = this.properties.filter(x => x !== 'id').map(x => ({
				name: x,
				label: x,
				value: this.content[x]
			}));
		}) || await engine.match(['fields', 'number'], async (_, o) => {
			o.template = 'Collection-Field';
		}) || await engine.match([undefined, 'control'], async (i, o) => {
			switch (i[0].name) {
				case 'components':
					const c = new ComponentList();
					c.name = i[0].name;
					c.items = i[0].value;
					c.selector = () => {
						return this.selector().querySelector('label[for="components"]').nextElementSibling;
					};
					o.value = this.componentList = c;
					break;
				default:
					o.template = 'Collection-Text';
					break;
			}
		});
	}

	listen = () => {
		this.selector().querySelector('.create')?.addEventListener('click', this.handleCreateClick);
		this.selector().querySelectorAll('.edit').forEach(x => x.addEventListener('click', this.handleEditClick));
		this.selector().querySelector('.save')?.addEventListener('click', this.handleSaveClick);
		this.componentList?.listen();
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async () => {
		this.content = {};
		this.refresh();
	}

	handleEditClick = async event => {
		const b = event.currentTarget;
		this.id = parseInt(b.value, 10);
		const s = await fetch(`/api/collections/${this.name}/${this.id}`);
		this.content = await s.json();
		this.refresh();
	}

	handleSaveClick = async event => {
		const b = event.currentTarget;
		event.preventDefault();
		const r = this.id ? `/api/collections/${this.name}/${this.id}` : `/api/collections/${this.name}`;
		await fetch(r, {
			method: this.id ? 'PUT' : 'POST',
			body: new URLSearchParams(new FormData(b.form, b))
		});
		delete this.id;
		delete this.content;
		delete this.componentList;
		this.refresh();
	}
}

export default Collection;
