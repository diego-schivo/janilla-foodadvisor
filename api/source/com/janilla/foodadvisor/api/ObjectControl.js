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
import ContentControl from './ContentControl.js';
import FileControl from './FileControl.js';
import ListControl from './ListControl.js';
import LocaleControl from './LocaleControl.js';
import TextControl from './TextControl.js';

class ObjectControl {

	selector;

	name;

	reference;

	type;

	engine;
	
	object;

	fields;

	control;

	controls;

	get level() {
		return ['even', 'odd'][this.engine.controls.length % 2];
	}

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.object = this.reference.getValue();
			if (this.object == null)
				o.template = 'ObjectControl-empty';
			else {
				const s = await fetch(`/api/types/${this.type}/properties`, {
					headers: engine.admin.apiHeaders
				});
				this.fields = [{ name: '$type', type: 'string' }, ...await s.json()];
				this.controls = [];
				o.template = 'ObjectControl';
			}
		}) || await engine.match(['fields', 'number'], async (_, o) => {
			const f = this.fields[o.key];
			const n = this.name != null ? `${this.name}.${f.name}` : f.name;
			o.value = {
				name: n,
				label: f.name
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
						c.types = f.referenceTypes ?? [f.typeArguments[0]];
						break;
					case 'long':
						if (f.referenceTypes)
							switch (f.referenceTypes[0]) {
								case 'File':
									c = new FileControl();
									break;
								default:
									c = new ContentControl();
									c.contentType = f.referenceTypes[0];
									break;
							}
						else
							c = new TextControl();
						break;
					case 'map':
						c = new LocaleControl();
						break;
					default:
						c = new TextControl();
						break;
				}
			c.selector = () => this.selector().querySelector(`label[for="${n}"] + *`);
			c.reference = {
				getValue: () => this.object[f.name],
				setValue: x => this.object[f.name] = x
			};
			c.name = n;
			this.control = c;
			this.controls.push(c);
		});
	}

	listen = () => {
		const e = this.selector();
		e.querySelector(':scope > .create')?.addEventListener('click', this.handleCreateClick);
		e.querySelector(':scope > .delete')?.addEventListener('click', this.handleDeleteClick);
		this.controls?.forEach(x => x.listen());
	}

	refresh = async () => {
		this.engine.stack.pop();
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async () => {
		this.reference.setValue({ $type: this.type });
		await this.refresh();
	}

	handleDeleteClick = async () => {
		this.object = null;
		await this.refresh();
	}
}

export default ObjectControl;
