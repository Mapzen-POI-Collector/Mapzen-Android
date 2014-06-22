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

package com.mapzen.data.osm;

import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.SettingsManager;


/**
 * Represents a changeset, used for data upload. 
 *
 */
public final class Changeset implements MapzenConstants {
 
	@SuppressWarnings("unused")
	private static final String TAG = Changeset.class.getSimpleName();
	
	private long _id;
    private boolean _open;
    private static Changeset _instance;
    
    public static Changeset getInstance() {
    	if (_instance == null)
    		_instance = new Changeset();
    	return _instance;
    }
    
    private Changeset() {
    	_open = false;
    	_id = -1;
    }

    public long getId() {
        return _id;
    }

	public void setId(long id) {
		_id = id;
	}

    public boolean isOpen() {
    	return _open;
    }

    public void setOpen(boolean open) {
        _open = open;
    }

    public boolean isNew() {
        return _id <= 0;
    }
    
    public void loadFromPrefs() {
		Long id = SettingsManager.getInstance().getLong("current_changeset", -1l);
		Boolean isOpen = SettingsManager.getInstance().getBoolean("current_changeset_state", false);
		setId(id);
		setOpen(isOpen);
	}
	
	public void saveToPrefs() {
		SettingsManager.getInstance().putLong("current_changeset", getId());
		SettingsManager.getInstance().putBoolean("current_changeset_state", isOpen());
	}
}
