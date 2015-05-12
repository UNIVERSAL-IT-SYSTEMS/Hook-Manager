/*
 * Copyright � 2014 - 2015 | Alexander01998 | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Updater
{
	public static final String CURRENT_VERSION = "0.1";
	
	private int currentMajor;
	private int currentMinor;
	private int currentPatch;
	private int currentPreRelease;
	
	private String latestVersion;
	private int latestMajor;
	private int latestMinor;
	private int latestPatch;
	private int latestPreRelease;
	
	private boolean outdated;
	private JsonArray json;
	
	public void checkForUpdate()
	{
		try
		{
			outdated = false;
			try
			{
				String[] parts = CURRENT_VERSION.split("\\.");
				currentMajor = Integer.parseInt(parts[0]);
				if(CURRENT_VERSION.contains("pre"))
					currentPreRelease =
						Integer.parseInt(CURRENT_VERSION
							.substring(CURRENT_VERSION.indexOf("pre") + 3));
				else
					currentPreRelease = 0;
				if(parts.length > 2)
					if(currentPreRelease == 0)
						currentPatch = Integer.parseInt(parts[2]);
					else
						currentPatch =
							Integer.parseInt(parts[2].substring(0,
								parts[2].indexOf("pre")));
				else
					currentPatch = 0;
				if(currentPreRelease == 0 || currentPatch > 0)
					currentMinor = Integer.parseInt(parts[1]);
				else
					currentMinor =
						Integer.parseInt(parts[1].substring(0,
							parts[1].indexOf("pre")));
			}catch(Exception e)
			{
				System.err.println("Current version \"" + CURRENT_VERSION
					+ "\" doesn't follow the semver.org syntax!");
				e.printStackTrace();
			}
			HttpsURLConnection connection =
				(HttpsURLConnection)new URL(
					"https://api.github.com/repos/Wurst-Imperium/Hook-Manager/releases")
					.openConnection();
			BufferedReader load =
				new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String content = load.readLine();
			for(String line = ""; (line = load.readLine()) != null;)
				content += "\n" + line;
			load.close();
			json = new JsonParser().parse(content).getAsJsonArray();
			JsonObject latestRelease = new JsonObject();
			for(JsonElement release : json)
				if(!release.getAsJsonObject().get("prerelease").getAsBoolean()
					|| currentPreRelease > 0)
				{
					latestRelease = release.getAsJsonObject();
					break;
				}
			try
			{
				latestVersion =
					latestRelease.get("tag_name").getAsString().substring(1);
				String[] parts = latestVersion.split("\\.");
				latestMajor = Integer.parseInt(parts[0]);
				if(latestVersion.contains("pre"))
					latestPreRelease =
						Integer.parseInt(latestVersion.substring(latestVersion
							.indexOf("pre") + 3));
				else
					latestPreRelease = 0;
				if(parts.length > 2)
					if(latestPreRelease == 0)
						latestPatch = Integer.parseInt(parts[2]);
					else
						latestPatch =
							Integer.parseInt(parts[2].substring(0,
								parts[2].indexOf("pre")));
				else
					latestPatch = 0;
				if(latestPreRelease == 0 || latestPatch > 0)
					latestMinor = Integer.parseInt(parts[1]);
				else
					latestMinor =
						Integer.parseInt(parts[1].substring(0,
							parts[1].indexOf("pre")));
			}catch(Exception e)
			{
				System.err.println("Latest version (\"" + latestVersion
					+ "\") doesn't follow the semver.org syntax!");
				e.printStackTrace();
			}
			try
			{
				outdated = isLatestVersionHigher();
			}catch(Exception e)
			{
				outdated = !latestVersion.equals(CURRENT_VERSION);
			}
			if(outdated)
				System.out.println("Update found: " + latestVersion);
			else
				System.out.println("No update found.");
		}catch(Exception e)
		{
			System.err.println("Unable to check for updates!");
			e.printStackTrace();
		}
	}
	
	private boolean isLatestVersionHigher()
	{
		if(latestMajor > currentMajor)
			return true;
		else if(latestMajor < currentMajor)
			return false;
		else if(latestMinor > currentMinor)
			return true;
		else if(latestMinor < currentMinor)
			return false;
		else if(latestPatch > currentPatch)
			return true;
		else if(latestPatch < currentPatch)
			return false;
		else if(latestPreRelease > currentPreRelease || latestPreRelease == 0
			&& currentPreRelease > 0)
			return true;
		else
			return false;
	}
	
	public void update()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(!new JsonParser().parse(
						new InputStreamReader(getClass().getClassLoader()
							.getResourceAsStream("auto-updater")))
						.getAsBoolean())
						return;
					File updater =
						new File(Updater.class.getProtectionDomain()
							.getCodeSource().getLocation().getPath());
					if(!updater.isDirectory())
						updater = updater.getParentFile();
					updater = new File(updater, "Wurst-updater.jar");
					updater =
						new File(updater.getAbsolutePath().replace("%20", " "));
					InputStream input =
						getClass().getClassLoader().getResourceAsStream(
							"Updater.jar");
					FileOutputStream output = new FileOutputStream(updater);
					byte[] buffer = new byte[8192];
					for(int length; (length = input.read(buffer)) != -1;)
						output.write(buffer, 0, length);
					input.close();
					output.close();
					String id;
					if(currentPreRelease > 0)
						id =
							json.get(0).getAsJsonObject().get("id")
								.getAsString();
					else
						id =
							new JsonParser()
								.parse(
									new InputStreamReader(
										new URL(
											"https://api.github.com/repos/Wurst-Imperium/Hook-Manager/releases/latest")
											.openStream())).getAsJsonObject()
								.get("id").getAsString();
					ProcessBuilder pb =
						new ProcessBuilder("cmd.exe", "/c", "java", "-jar",
							updater.getAbsolutePath(), "update", id, updater
								.getParentFile().getAbsolutePath()
								.replace(" ", "%20"));
					pb.redirectErrorStream(true);
					Process p = pb.start();
					BufferedReader pInput =
						new BufferedReader(new InputStreamReader(p
							.getInputStream()));
					for(String message; (message = pInput.readLine()) != null;)
						System.out.println(message);
					pInput.close();
				}catch(Exception e)
				{
					System.err.println("Could not update!");
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public boolean isOutdated()
	{
		return outdated;
	}
	
	public String getLatestVersion()
	{
		return latestVersion;
	}
}
