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

package com.mapzen.configuration;

import java.util.ArrayList;
import java.util.Map;

public class TypeItem extends AbstractItem implements Comparable<TypeItem> {
	
	private AbstractItem _config;
	private AbstractItem _defaultConfig;

	private String _name;
	
	public TypeItem(String name) {
		_name = name;
	}
	
	public void setConfiguration(AbstractItem item) {
		_config = item;
	}
	
	public void setDefaultConfiguration(AbstractItem item) {
		_defaultConfig = item;
	}
	
	public String getName() {
		return _name;
	}
	
	public ArrayList<TagItem> getTagging() {
		if (_defaultConfig != null)
			return _defaultConfig.getTagging();
		else
			return _config.getTagging();
	}

	@Override
	public boolean match(Map<String, String> tagsCollection) {
		boolean result = _config.match(tagsCollection);
		if (result) {
			ArrayList<TagItem> tagsToRemove = _config.getTagging();
			if (tagsToRemove != null) {
				for (TagItem tagToRemove: tagsToRemove) {
					tagsCollection.remove(tagToRemove.getKey());
				}
			}
		}
		return result;
	}

	@Override
	public int compareTo(TypeItem another) {
		return getName().compareTo(another.getName());
	}	
}
