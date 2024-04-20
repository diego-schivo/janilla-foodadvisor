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
import ListControl from './ListControl.js';
import MapControl from './MapControl.js';
import TextControl from './TextControl.js';

class ObjectControl {

	selector;

	name;

	binding;

	type;

	engine;

	fields;

	control;

	controls;

	get value() {
		return this.binding.getter();
	}

	set value(x) {
		this.binding.setter(x);
	}

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			if (this.value == null)
				o.template = 'ObjectControl-empty';
			else {
				const s = await fetch(`/api/types/${this.type}/properties`, {
					headers: engine.app.apiHeaders
				});
				this.fields = [{ name: '$type', type: 'string' }, ...await s.json()];
				this.controls = [];
				o.template = 'ObjectControl';
			}
		}) || await engine.match(['fields', 'number'], async (_, o) => {
			const f = this.fields[o.key];
			const n = this.name != null ? `${this.name}.${f.name}` : f.name;
			const v = this.value[f.name];
			o.value = {
				name: n,
				label: f.name,
				value: v
			};
			o.template = 'ObjectControl-Field';
			let c;
			if (/^[A-Z]/.test(f.type)) {
				c = new ObjectControl();
				c.type = f.type;
			}
			else
				switch (f.type) {
					case 'list':
						c = new ListControl();
						if (f.typeArguments?.length > 0)
							c.types = f.typeArguments[0];
						break;
					case 'map':
						c = new MapControl();
						break;
					default:
						c = new TextControl();
						break;
				}
			c.selector = () => this.selector().querySelector(`label[for="${n}"]`).nextElementSibling;
			c.binding = {
				getter: () => this.value[f.name],
				setter: x => this.value[f.name] = x
			};
			c.name = n;
			this.control = c;
			this.controls.push(c);
		});
	}

	listen = () => {
		const e = this.selector();
		e.querySelector(`.create[name="${this.name}"]`)?.addEventListener('click', this.handleCreateClick);
		e.querySelector(`.delete[name="${this.name}"]`)?.addEventListener('click', this.handleDeleteClick);
		this.controls?.forEach(x => x.listen());
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async event => {
		event.preventDefault();
		this.value = { $type: this.type };
		await this.refresh();
	}

	handleDeleteClick = async event => {
		event.preventDefault();
		this.value = null;
		await this.refresh();
	}
}

export default ObjectControl;
