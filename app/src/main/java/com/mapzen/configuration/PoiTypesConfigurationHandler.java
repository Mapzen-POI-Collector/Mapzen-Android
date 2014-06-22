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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import android.util.Log;

public class PoiTypesConfigurationHandler extends DefaultHandler {

    private final static String TAG = PoiTypesConfigurationHandler.class.getSimpleName();

    OsmDriver osmNodeTypeResolver = OsmDriver.getInstance();

    private final static String typeTag = "type";
    private final static String tagTag = "tag";
    private final static String orTag = "or";
    private final static String andTag = "and";
    private final static String defaultTag = "default";

    private final static String keyAttr = "key";
    private final static String valueAttr = "value";
    private final static String nameAttr = "name";

    //service variables
    private String nameAttibuteValue;
    private String keyAttributeValue;
    private String valueAttributyValue;

    private boolean orFlag = false;
    private boolean andFlag = false;
    private boolean defaultFlag = false;
    private TypeItem currentType;
    private OrItem orItem;
    private AndItem andItem;
    private DefaultItem defaultItem;
    private TagItem tagItem;

    @Override
    public void startDocument() throws SAXException {
        Log.d(TAG, "configuration_start");
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d(TAG, "configuration_end");
    }

    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {

        if (localName.equals(typeTag)) {
            nameAttibuteValue = atts.getValue(nameAttr);
            currentType = new TypeItem(nameAttibuteValue);
        }
        if (localName.equals(orTag)) {
            orItem = new OrItem();
            addToParent(orItem);
            orFlag = true;
        }
        if (localName.equals(andTag)) {
            andItem = new AndItem();
            addToParent(andItem);
            andFlag = true;
        }
        if (localName.equals(defaultTag)) {
            defaultItem = new DefaultItem();
            currentType.setDefaultConfiguration(defaultItem);
            defaultFlag = true;
        }
        if (localName.equals(tagTag)) {
            keyAttributeValue = atts.getValue(keyAttr);
            valueAttributyValue = atts.getValue(valueAttr);
            tagItem = new TagItem(keyAttributeValue, valueAttributyValue);
            addToParent(tagItem);
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName,
                           String qName) throws SAXException {

        if (localName.equals(typeTag)) {
            osmNodeTypeResolver.addType(currentType);
        }
        if (localName.equals(orTag)) {
            orFlag = false;
        }
        if (localName.equals(andTag)) {
            andFlag = false;
        }
        if (localName.equals(defaultTag)) {
            defaultFlag = false;
        }
    }

    private void addToParent(AbstractItem item) {
        if (orFlag) {
            if (andFlag) {
                //or+and
                andItem.addItem(item);
            } else {
                //or
                orItem.addItem(item);
            }
        } else {
            if (andFlag) {
                if (defaultFlag) {
                    //and+default
                    andItem.addItem(item);
                } else {
                    //and
                    andItem.addItem(item);
                }
            } else {
                if (defaultFlag) {
                    defaultItem.setChildItem(item);
                } else {
                    //no flags, just add tag to parent
                    currentType.setConfiguration(item);
                }
            }
        }
    }
}

