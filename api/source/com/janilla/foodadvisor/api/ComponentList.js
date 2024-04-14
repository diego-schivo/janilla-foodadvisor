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
class ComponentList {

	list;

	selector;

	engine;

	index;

	item;

	properties;

	fields;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			o.template = 'ComponentList';
		}) || (this.list && await engine.match([this.list, 'number'], async (_, o) => {
			this.index = o.key;
			this.item = o.value;
			const s = await fetch(`/api/types/${this.item['$type']}/properties`);
			this.properties = await s.json();
			o.template = 'ComponentList-Item';
		})) || (this.item && await engine.match([this.item, 'fields'], async (_, o) => {
			o.value = this.fields = ['$type', ...this.properties].map(x => ({ name: x, value: this.item[x] }));
		})) || (this.fields && await engine.match([this.fields, 'number'], async (_, o) => {
			o.template = 'ComponentList-Field';
		})) || await engine.match(['name'], async (_, o) => {
			o.value = `components[${this.index}].${o.value}`;
		}) || await engine.match(['control'], async (_, o) => {
			o.template = 'Collection-Text';
		}) || await engine.match(['options'], async (_, o) => {
			const s = await fetch('/api/components');
			o.value = await s.json();
		}) || await engine.match(['options', 'number'], async (_, o) => {
			o.template = 'ComponentList-Option';
		});
	}

	listen = () => {
		this.selector().querySelector('.type').addEventListener('change', this.handleTypeChange);
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleTypeChange = async event => {
		const t = event.currentTarget.value;
		if (!this.list)
			this.list = [];
		this.list.push({ '$type': t });
		this.refresh();
	}
}

export default ComponentList;
