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
class FileControl {

	selector;
	
	binding;

	engine;
	
	value;
	
	request;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.value = this.binding.getter();
			o.template = 'FileControl';
		}) || await engine.match(['dialog'], async (_, o) => {
			o.template = 'FileControl-Dialog';
		});
	}

	listen = () => {
		this.selector().querySelector('button').addEventListener('click', this.handleButtonClick);
	}
	
	handleButtonClick = event => {
		event.preventDefault();
		const d = this.selector().querySelector('dialog');
		d.querySelector('input').addEventListener('change', this.handleFileChange);
		d.showModal();
	}
	
	handleFileChange = event => {
		const i = event.currentTarget;
		const d = new FormData();
		d.append(i.name, i.files[0]);
		const r = this.request = new XMLHttpRequest();
		r.addEventListener('load', this.handleRequestLoad);
		r.open('POST', '/admin/upload', true);
		Object.entries(this.engine.app.apiHeaders).forEach(([h, v]) => r.setRequestHeader(h, v));
		r.send(d);
	}
	
	handleRequestLoad = () => {
		const e = this.selector();
		e.querySelector('dialog').close();
		e.querySelector('input').value = this.value = this.request.responseText;
		this.binding.setter(this.value);
	}
}

export default FileControl;
