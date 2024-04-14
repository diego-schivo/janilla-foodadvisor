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
class List {

	items;

	selector;

	engine;

	index;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			delete this.index;
			o.template = 'List';
		}) || (this.items && await engine.match([this.items, 'number'], async (_, o) => {
			this.index = o.key;
			o.template = 'List-Item';
		})) || await engine.match(['field'], async (_, o) => {
			o.value = {
				name: `components[0].images[${this.index}]`,
				label: this.index,
				value: this.items[this.index]
			};
			o.template = 'List-Field';
		}) || await engine.match(['control'], async (_, o) => {
			o.template = 'List-Text';
		});
	}

	listen = () => {
		this.selector().addEventListener('change', this.handleChange);
		this.selector().querySelector(':scope > .add').addEventListener('click', this.handleAddClick);
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleChange = async event => {
		const i = event.target.closest('input');
		if (!i)
			return;
		const g = i.name.match(/\[(\d+)\]$/)[1];
		const j = parseInt(g, 10);
		this.items[j] = i.value;
	}

	handleAddClick = async () => {
		if (!this.items)
			this.items = [];
		this.items.push('');
		this.refresh();
	}
}

export default List;
