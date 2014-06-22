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
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PoiCategoriesConfigurationHandler extends DefaultHandler {

    private HashMap<String,ArrayList<String>> categoryToTypeMapping;
    private String currentCategory;

    public HashMap<String,ArrayList<String>> getMapping() {
        return categoryToTypeMapping;
    }

    @Override
    public void startDocument() throws SAXException {
        categoryToTypeMapping = new HashMap<String, ArrayList<String>>();
    }

    @Override
    public void endDocument() throws SAXException {}

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (localName.equals("category")) {
            currentCategory = attributes.getValue("name");
        }
        if (localName.equals("type")) {
            if(categoryToTypeMapping.containsKey(currentCategory))
                categoryToTypeMapping.get(currentCategory).add(attributes.getValue("name"));
            else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(attributes.getValue("name"));
                categoryToTypeMapping.put(currentCategory, list);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }
}
