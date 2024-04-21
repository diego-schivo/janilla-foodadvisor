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
import TextControl from './TextControl.js';

class LocaleControl {

	selector;

	name;

	reference;

	engine;
	
	value;

	entries;

	field;

	controls;

	get level() {
		return ['even', 'odd'][this.engine.controls.length % 2];
	}

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.value = this.reference.getValue();
			if (this.value == null) {
				['entries', 'field', 'controls'].forEach(x => delete this[x]);
				o.template = 'LocaleControl';
			} else {
				this.entries = engine.admin.availableLocales.map(x => [x, this.value[x]]);
				this.controls = [];
				o.template = 'LocaleControl-existing';
			}
		}) || (this.entries && await engine.match([this.entries, 'number'], async (_, o) => {
			const l = o.value[0];
			const n = `${this.name}.${l}`;
			const c = new TextControl();
			c.selector = () => this.selector().querySelector(`label[for="${n}"]`).nextElementSibling;
			c.reference = {
				getValue: () => this.value[l],
				setValue: x => this.value[l] = x
			};
			c.name = n;
			this.field = {
				label: l,
				name: n,
				control: c
			};
			this.controls.push(c);
			o.value = engine.admin.locales.includes(l) ? {} : { hidden: 'hidden' };
			o.template = 'LocaleControl-Entry';
		})) || (this.field && await engine.match([this.field], async (_, o) => {
			o.template = 'LocaleControl-Field';
		}));
	}

	listen = () => {
		this.selector().querySelector(':scope > [name="create"]')?.addEventListener('click', this.handleCreateClick);
		this.controls?.forEach(x => x.listen());
	}

	refresh = async () => {
		this.engine.stack.pop();
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async () => {
		this.reference.setValue({});
		await this.refresh();
	}
}

export default LocaleControl;
