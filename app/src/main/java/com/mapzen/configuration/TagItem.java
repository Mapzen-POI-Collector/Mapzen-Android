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
import java.util.Map.Entry;

public class TagItem extends AbstractItem {

    private String _key;
    private String _value;

    public TagItem(String key, String value) {
        _key = key;
        _value = value;
    }

    public String getKey() {
        return _key;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public boolean match(Map<String, String> tagsCollection) {
        for (Entry<String,String> entry : tagsCollection.entrySet()) {
            if ((entry.getKey().equals(_key))&&(entry.getValue().equals(_value)))
                return true;
        }
        return false;
    }

    @Override
    public ArrayList<TagItem> getTagging() {
        ArrayList<TagItem> result = new ArrayList<TagItem>(1);
        result.add(this);
        return result;
    }

}
