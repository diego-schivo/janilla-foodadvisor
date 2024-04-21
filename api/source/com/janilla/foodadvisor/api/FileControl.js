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
	
	reference;

	engine;
	
	value;
	
	request;

	render = async engine => {
		return await engine.match([this], async (_, o) => {
			this.engine = engine.clone();
			this.value = this.reference.getValue();
			o.template = 'FileControl';
		});
	}

	listen = () => {
		const e = this.selector();
		e.querySelector(':scope > .upload').addEventListener('click', this.handleUploadClick);
		const d = e.querySelector(':scope > .upload-dialog');
		d?.querySelector('input').addEventListener('change', this.handleFileChange);
		d?.addEventListener('close', this.handleDialogClose);
	}

	refresh = async () => {
		this.selector().outerHTML = await this.engine.render({ value: this });
		this.listen();
	}
	
	handleUploadClick = async () => {
		const h = await this.engine.render({ template: 'FileControl-uploadDialog' });
		const e = this.selector();
		e.insertAdjacentHTML('beforeend', h);
		e.querySelector(':scope > .upload-dialog').showModal();
		this.listen();
	}
	
	handleFileChange = event => {
		const d = new FormData();
		const i = event.currentTarget;
		d.append(i.name, i.files[0]);
		const r = this.request = new XMLHttpRequest();
		r.addEventListener('load', this.handleRequestLoad);
		r.open('POST', '/admin/upload', true);
		Object.entries(this.engine.admin.apiHeaders).forEach(([h, v]) => r.setRequestHeader(h, v));
		r.send(d);
	}
	
	handleDialogClose = async () => {
		await this.refresh();
	}
	
	handleRequestLoad = () => {
		this.reference.setValue(this.value = this.request.responseText);
		this.selector().querySelector(':scope > .upload-dialog').close();
	}
}

export default FileControl;
