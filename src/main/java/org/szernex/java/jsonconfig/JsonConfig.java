/*
The MIT License (MIT)

Copyright (c) 2016 Szernex

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package org.szernex.java.jsonconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonConfig<ConfigObject> {
	private static final Logger logger = LogManager.getLogger(JsonConfig.class);

	private final Class<ConfigObject> configObjectClass;

	public JsonConfig(Class<ConfigObject> configObjectClass) {
		this.configObjectClass = configObjectClass;
	}

	public ConfigObject load(Path path) {
		if (!Files.exists(path)) {
			try {
				logger.info("Creating new empty config file " + path.toString());

				ConfigObject empty_config = configObjectClass.newInstance();

				if (!save(empty_config, path)) {
					logger.error("Could not save empty config file");
				}

				return empty_config;
			} catch (InstantiationException | IllegalAccessException ex) {
				logger.error("Error creating ConfigObject instance: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		try {
			StringBuilder raw = new StringBuilder();

			Files.readAllLines(path).forEach(raw::append);

			Gson gson = new Gson();
			ConfigObject config = gson.fromJson(raw.toString(), configObjectClass);

			System.out.println("JsonConfig file " + path.toString() + " loaded");

			return config;
		} catch (IOException ex) {
			System.err.println("Error reading config file " + path.toString() + ": " + ex.getMessage());
			ex.printStackTrace();

			return null;
		}
	}

	public boolean save(ConfigObject config, Path path) {
		if (!Files.exists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException ex) {
				logger.error("Error creating config file " + path + ": " + ex.getMessage());
				ex.printStackTrace();

				return false;
			}
		}

		try {
			Gson gson = new GsonBuilder()
					.setPrettyPrinting()
					.create();

			Files.write(path, gson.toJson(config).getBytes(StandardCharsets.UTF_8));

			return true;
		} catch (IOException ex) {
			logger.error("Error writing config to file " + path + ": " + ex.getMessage());
			ex.printStackTrace();

			return false;
		}
	}
}