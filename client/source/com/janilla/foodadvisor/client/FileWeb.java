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
package com.janilla.foodadvisor.client;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import com.janilla.foodadvisor.api.File;
import com.janilla.http.HttpResponse;
import com.janilla.http.HttpResponse.Status;
import com.janilla.io.IO;
import com.janilla.persistence.Persistence;
import com.janilla.web.Handle;
import com.janilla.web.NotFoundException;

public class FileWeb {

	Persistence persistence;

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	@Handle(method = "GET", path = "/files/(\\d+)")
	public void getFile(long id, HttpResponse response) throws IOException {
		var f = persistence.getCrud(File.class).read(id);
		if (f == null)
			throw new NotFoundException();
		response.setStatus(new Status(200, "OK"));
		var n = f.name;
		var e = n.substring(n.lastIndexOf('.') + 1);
		var hh = response.getHeaders();
		switch (e) {
		case "png":
			hh.set("Content-Type", "image/png");
			break;
		}
		var bb = f.bytes;
		hh.set("Content-Length", String.valueOf(bb.length));
		var b = (WritableByteChannel) response.getBody();
		IO.write(bb, b);
	}
}
