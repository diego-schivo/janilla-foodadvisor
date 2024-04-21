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
import Content from './Content.js';

class ContentControl {

	selector;

	name;

	reference;
	
	contentType;

	engine;

	value;

	content;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.value = this.reference.getValue();
			o.template = this.value != null ? 'ContentControl' : 'ContentControl-unset';
		});
	}

	listen = () => {
		const e = this.selector();
		e.querySelector(':scope > .create')?.addEventListener('click', this.handleCreateClick);
		e.querySelector(':scope > input')?.addEventListener('change', this.handleInputChange);
		e.querySelector(':scope > .search')?.addEventListener('click', this.handleSearchClick);
		const s = e.querySelector(':scope > .search-dialog');
		s?.addEventListener('contentselect', this.handleContentSelect);
		s?.addEventListener('close', this.handleSearchClose);
		this.content?.listen();
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}

	handleCreateClick = async () => {
		this.value = '';
		this.reference.setValue(this.value);
		await this.refresh();
	}

	handleInputChange = event => {
		this.reference.setValue(this.value = event.currentTarget.value);
	}

	handleSearchClick = async () => {
		const c = new Content();
		c.selector = () => this.selector().querySelector(':scope > .search-dialog > *');
		c.mode = 'select';
		c.typeReference = { getType: () => this.contentType };
		this.content = c;
		const h = await this.engine.render({ template: 'ContentControl-searchDialog' });
		const e = this.selector();
		e.insertAdjacentHTML('beforeend', h);
		e.querySelector(':scope > .search-dialog').showModal();
		this.listen();
	}
	
	handleContentSelect = async event => {
		event.currentTarget.close();
		this.reference.setValue(this.value = event.detail.id);
	}
	
	handleSearchClose = async () => {
		delete this.content;
		await this.refresh();
	}
}

export default ContentControl;
