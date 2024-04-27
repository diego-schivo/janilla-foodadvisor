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
class Gallery {

	selector;

	index;

	length;

	listen = () => {
		const e = this.selector();
		const ii = [...e.querySelectorAll('li')];
		ii.slice(-2).forEach(x => ii[0].before(x.cloneNode(true)));
		ii.slice(0, 2).reverse().forEach(x => ii.at(-1).after(x.cloneNode(true)));
		for (let i = 0; i < 2; i++)
			e.insertAdjacentHTML('beforeend', '<button type="button"></button>');
		e.insertAdjacentHTML('beforeend', '<ul>' + ii.map(() => '<li><button type="button"></button></li>').join('') + '</ul>');
		this.index = 0;
		this.length = ii.length;
		this.selector().querySelector(':scope > button:first-of-type').addEventListener('click', this.handleLeftClick);
		this.selector().querySelector(':scope > button:nth-of-type(2)').addEventListener('click', this.handleRightClick);
		this.selector().querySelector('ul:nth-of-type(2)').addEventListener('click', this.handleDotClick);
		this.foo();
	}

	handleLeftClick = () => {
		this.index = (this.index + this.length - 1) % (this.length);
		this.foo();
	}

	handleRightClick = () => {
		this.index = (this.index + 1) % (this.length);
		this.foo();
	}

	handleDotClick = event => {
		const b = event.target.closest('button');
		if (!b)
			return;
		this.index = [...event.currentTarget.children].indexOf(b.parentElement);
		this.foo();
	}

	foo = () => {
		const e = this.selector();
		e.querySelector('ul:first-of-type').style.translate = `calc(-${2 + this.index} * 100% / 3)`;
		[...e.querySelectorAll('ul:nth-of-type(2) button')].forEach((f, i) => f.style.background = i === this.index ? 'rgb(8, 8, 8)' : null);
	}
}

export default Gallery;
