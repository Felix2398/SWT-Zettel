/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.mhisoft.wallet.service;

import java.awt.Dimension;
import java.io.*;
import java.util.Collections;

import javax.swing.JSplitPane;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.mhisoft.wallet.SystemSettings;
import org.mhisoft.wallet.model.WalletSettings;

/**
 * Description: service for the settings.
 *
 * @author Tony Xue
 * @since Apr, 2016
 */
public class WalletSettingsService {
	//user.dir --> app launch dir

	private static final String userHome = System.getProperty("user.home") + File.separator;
	private static final String filePath = userHome + "eVaultSettings.json";

	public enum SettingsKeys implements JsonKey {
		FONT_SIZE,
		DIMENSION_X,
		DIMENSION_Y,
		DIVIDER_LOCATION,
		LAST_FILE,
		IDLE_TIMEOUT,
		RECENT_FILES,
		TREE_EXPAND,
		RECENT_OPEN_DIR
		;

		@Override
		public String getKey() {
			return this.name().toLowerCase();
		}

		@Override
		public Object getValue() {
			return null;
		}
	}

	/**
	 * Save the settings to file
	 * @param settings
	 */
	public void saveSettingsToFile(WalletSettings settings) {
		try(BufferedWriter out = new BufferedWriter(new FileWriter(filePath))) {
			if (SystemSettings.isDevMode) {
				settings.setIdleTimeout(-1);
			}

			JsonObject jsObject = new JsonObject();

			jsObject.put(SettingsKeys.FONT_SIZE.getKey(), settings.getFontSize());
			jsObject.put(SettingsKeys.DIMENSION_X.getKey(), settings.getDimensionX());
			jsObject.put(SettingsKeys.DIMENSION_Y.getKey(), settings.getDimensionY());
			jsObject.put(SettingsKeys.DIVIDER_LOCATION.getKey(), settings.getDividerLocation());
			jsObject.put(SettingsKeys.LAST_FILE.getKey(), settings.getLastFile());
			jsObject.put(SettingsKeys.IDLE_TIMEOUT.getKey(), settings.getIdleTimeout());
			jsObject.put(SettingsKeys.RECENT_FILES.getKey(), settings.getRecentFiles());
			jsObject.put(SettingsKeys.TREE_EXPAND.getKey(), settings.isTreeExpanded());
			jsObject.put(SettingsKeys.RECENT_OPEN_DIR.getKey(), settings.getRecentOpenDir());

			jsObject.toJson(out);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public WalletSettings readSettingsFromFile() {

		try(BufferedReader in = new BufferedReader(new FileReader(filePath))) {
			JsonObject jsonObject = (JsonObject) Jsoner.deserialize(in);
			WalletSettings settings = new WalletSettings();

			settings.setFontSize(jsonObject.getInteger(SettingsKeys.FONT_SIZE));
			settings.setDimensionX(jsonObject.getInteger(SettingsKeys.DIMENSION_X));
			settings.setDimensionY(jsonObject.getInteger(SettingsKeys.DIMENSION_Y));
			settings.setDividerLocation(jsonObject.getDouble(SettingsKeys.DIVIDER_LOCATION));
			settings.setLastFile(jsonObject.getString(SettingsKeys.LAST_FILE));
			settings.setIdleTimeout(jsonObject.getLong(SettingsKeys.IDLE_TIMEOUT));

			/*
			Es ist keine Setter-Methode für recentFiles vorhanden. Eine mögliche Lösung wäre es die addRecentFile()
			Methode für jeden Dateinamen in der Liste aufzurufen, was aber zu weiteren Fehlern führen würde.
			Die Persistierung der Daten über JSON war hier von den ursprünglichen Entwicklern nicht eingeplant.
			Deshalb ist es nicht möglich die recentFiles wieder ohne Fehler einzulesen.
			*/

			settings.setTreeExpanded(jsonObject.getBoolean(SettingsKeys.TREE_EXPAND));
			settings.setRecentOpenDir(jsonObject.getString(SettingsKeys.RECENT_OPEN_DIR));

			ServiceRegistry.instance.registerSingletonService(settings);
			return settings;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}


	public void updateAndSavePreferences() {
		//save the settings
		Dimension d = ServiceRegistry.instance.getWalletForm().getFrame().getSize();
		WalletSettings settings = ServiceRegistry.instance.getWalletSettings();
		settings.setDimensionX(d.width);
		settings.setDimensionY(d.height);

		//calculate proportion
		JSplitPane split = ServiceRegistry.instance.getWalletForm().getSplitPanel();
		double p = Double.valueOf(split.getDividerLocation()).doubleValue() / Double.valueOf(split.getWidth() - split.getDividerSize());
		settings.setDividerLocation(Double.valueOf(p * 100 + 0.5).intValue() / Double.valueOf(100));

		ServiceRegistry.instance.getWalletSettingsService().saveSettingsToFile(settings);
	}


}
