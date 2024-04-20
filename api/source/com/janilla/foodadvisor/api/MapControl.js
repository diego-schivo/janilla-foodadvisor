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
class MapControl {

	selector;

	name;

	binding;

	engine;

	entries;

	entry;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			const v = this.binding.getter();
			this.entries = v != null ? Object.entries(v) : null;
			o.template = 'MapControl';
		}) || (this.entries && await engine.match([this.entries, 'number'], async (_, o) => {
			o.template = 'MapControl-Entry';
		}));
	}

	listen = () => {
		const e = this.selector();
		e.querySelectorAll(':scope > ul > li > input:not([type="hidden"])').forEach(x => x.addEventListener('change', this.handleInputChange));
		e.querySelector(':scope > .add').addEventListener('click', this.handleAddClick);
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleInputChange = event => {
		var t = event.currentTarget;
		const p = t.parentElement;
		const e = this.entries[[...p.parentElement.children].indexOf(p)];
		var h = p.querySelector(':scope > input[type="hidden"]');
		if (t.classList.contains('key')) {
			e[0] = t.value;
			h.name = `${this.name}.${t.value}`;
		} else
			h.value = e[1] = t.value;
		this.binding.setter(Object.fromEntries(this.entries));
	}

	handleAddClick = async event => {
		event.preventDefault();
		if (!this.entries)
			this.entries = [];
		this.entries.push(['', '']);
		this.binding.setter(Object.fromEntries(this.entries));
		await this.refresh();
	}
}

export default MapControl;
