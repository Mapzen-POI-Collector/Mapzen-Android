/*Copyright (c) 2011-2012, Cloudmade
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/

package com.mapzen.data;


import android.util.Log;

public class StatisticManager {
	
	private static int created;
	private static int updated;
	private static int deleted;
	
	public static final String NODES_CREATED = "nodes_created";
	public static final String NODES_UPDATED = "nodes_updated";
	public static final String NODES_DELETED = "nodes_deleted";
	
	public static int getCreated() {
		return created;
	}
	
	public static int getUpdated() {
		return updated;
	}
	
	public static int getDeleted() {
		return deleted;
	}
	
	public static void logCreated() {
		created++;
	}
	
	public static void logUpdated() {
		updated++;
	}
	
	public static void logDeleted() {
		deleted++;
	}
	
	public static void loadFromPreferences() {
		try {
		SettingsManager mgr = SettingsManager.getInstance();
		created = mgr.getInt(NODES_CREATED, 0);
		updated = mgr.getInt(NODES_UPDATED, 0);
		deleted = mgr.getInt(NODES_DELETED, 0);	
		}
		catch (Exception e) {
			Log.e(StatisticManager.class.getSimpleName(), "Error while trying to load statistics from prefs");
			e.printStackTrace();
		}
	}
	
	public static void saveToPreferences() {
		SettingsManager mgr = SettingsManager.getInstance();
		mgr.putInt(NODES_CREATED, created);
		mgr.putInt(NODES_UPDATED, updated);
		mgr.putInt(NODES_DELETED, deleted);
	}
}
