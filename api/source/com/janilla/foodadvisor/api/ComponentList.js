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
import List from './List.js';

class ComponentList {
	
	name;

	items;

	selector;

	engine;

	index;

	properties;

	fields;
	
	fieldIndex;
	
	controls;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			['index', 'properties', 'fields', 'fieldIndex', 'list'].forEach(x => delete this[x]);
			this.controls = [];
			o.template = 'ComponentList';
		}) || (this.items && await engine.match([this.items, 'number'], async (_, o) => {
			this.index = o.key;
			const s = await fetch(`/api/types/${this.items[this.index]['$type']}/properties`);
			this.properties = await s.json();
			o.template = 'ComponentList-Item';
		})) || (this.index >= 0 && await engine.match([this.items[this.index], 'fields'], async (_, o) => {
			o.value = this.fields = ['$type', ...this.properties].map(x => ({
				name: `${this.name}[${this.index}].${x}`,
				label: x,
				value: this.items[this.index][x]
			}));
		})) || (this.fields && await engine.match([this.fields, 'number'], async (_, o) => {
			this.fieldIndex = o.key;
			o.template = 'ComponentList-Field';
		})) || await engine.match(['control'], async (_, o) => {
			const f = this.fields[this.fieldIndex];
			let c;
			switch (f.label) {
				case 'images':
					c = new List();
					c.items = f.value;
					c.selector = () => this.selector().querySelector(`label[for="${f.name}"]`).nextElementSibling;
					this.controls.push(c);
					o.value = c;
					break;
				case 'buttons':
					c = new ComponentList();
					c.name = f.name;
					c.items = f.value;
					c.selector = () => this.selector().querySelector(`label[for="${f.name}"]`).nextElementSibling;
					this.controls.push(c);
					o.value = c;
					break;
				default:
					o.template = 'ComponentList-Text';
					break;
			}
		}) || await engine.match(['options'], async (_, o) => {
			const s = await fetch('/api/components');
			o.value = await s.json();
		}) || await engine.match(['options', 'number'], async (_, o) => {
			o.template = 'ComponentList-Option';
		});
	}

	listen = () => {
		this.selector().querySelector(':scope > .add').addEventListener('click', this.handleAddClick);
		this.controls.forEach(x => x.listen());
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleAddClick = async event => {
		event.preventDefault();
		const d = this.selector().querySelector(':scope > .add-dialog');
		d.showModal();
		d.querySelector('.type').addEventListener('change', this.handleTypeChange);
	}

	handleTypeChange = async event => {
		const s = event.currentTarget;
		s.closest('dialog').close();
		if (!this.items)
			this.items = [];
		this.items.push({ '$type': s.value });
		this.refresh();
	}
}

export default ComponentList;
