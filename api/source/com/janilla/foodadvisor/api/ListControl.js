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
import FileControl from './FileControl.js';
import ObjectControl from './ObjectControl.js';
import TextControl from './TextControl.js';

class ListControl {

	selector;

	name;

	binding;

	types;

	engine;
	
	items;

	control;

	controls;

	render = async engine => {
		let ii;
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.items = this.binding.getter();
			this.controls = [];
			o.template = 'ListControl';
		}) || (this.items && await engine.match([this.items, 'number'], async (_, o) => {
			const i = o.key;
			const n = `${this.name}[${i}]`;
			const c = Object.hasOwn(this.items[i], '$type') ? new ObjectControl() : (this.name.endsWith('images') ? new FileControl() : new TextControl());
			c.selector = () => this.selector().querySelector(`.item:nth-child(${1 + i})`).firstElementChild;
			c.name = n;
			c.binding = {
				getter: () => this.items[i],
				setter: x => this.items[i] = x
			};
			if (this.types)
				c.type = this.items[i].$type;
			this.control = c;
			this.controls.push(c);
			o.template = 'ListControl-Item';
		})) || await engine.match(['dialog'], async (_, o) => {
			if (this.types)
				o.template = 'ListControl-Dialog';
		}) || (this.types && await engine.match([this.types, 'number'], async (_, o) => {
			o.template = 'ListControl-Option';
		}));
	}

	listen = () => {
		this.selector().querySelector(`.add[name="${this.name}"]`).addEventListener('click', this.handleAddClick);
		this.controls.forEach(x => x.listen());
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleAddClick = async event => {
		event.preventDefault();
		if (this.types.length === 1) {
			await this.add(/^[A-Z]/.test(this.types[0]) ? { '$type': this.types[0] } : '');
		} else {
			const d = this.selector().querySelector(':scope > .dialog');
			d.querySelector('.type').addEventListener('change', this.handleTypeChange);
			d.showModal();
		}
	}

	handleTypeChange = async event => {
		const s = event.currentTarget;
		s.closest('.dialog').close();
		await this.add({ '$type': s.value });
	}

	add = async item => {
		if (!this.items)
			this.items = [];
		this.items.push(item);
		this.binding.setter(this.items);
		await this.refresh();
	}
}

export default ListControl;
