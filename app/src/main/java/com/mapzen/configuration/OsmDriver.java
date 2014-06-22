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

/**
 * 
 */
package com.mapzen.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.mapzen.data.ResourceManager;

import android.util.Log;

/**
 * @author Vitalii Grygoruk
 * 
 */
public class OsmDriver {

	private static OsmDriver _instance;

	private static ArrayList<TypeItem> _nodeTypes;

	private static SAXParserFactory spf;
	private SAXParser sp;

	private static HashMap<String, ArrayList<String>> categoryToTypeMapping;

	public static OsmDriver getInstance() {
		if (_instance == null) {
			_instance = new OsmDriver();
		}
		return _instance;
	}

	private OsmDriver() {
		spf = SAXParserFactory.newInstance();
		try {
			sp = spf.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		_nodeTypes = new ArrayList<TypeItem>();
	}

	public void addType(TypeItem type) {
		_nodeTypes.add(type);
	}
	
	public List<String> getCategories() {
		List<String> categoriesList = new ArrayList<String>(categoryToTypeMapping.keySet());
		Collections.sort(categoriesList);
		return categoriesList;
	}
	
	public List<String> getTypes(String category) {
		List<String> typesList = categoryToTypeMapping.get(category); 
		Collections.sort(typesList);
		return typesList;
	}

	public void init(InputStream typesInputStream, InputStream categoryToTypeInputStream) {
		try {
			XMLReader xmlReader = sp.getXMLReader();
			PoiTypesConfigurationHandler handler = new PoiTypesConfigurationHandler();
			xmlReader.setContentHandler(handler);
			InputSource ins = new InputSource(
					new InputStreamReader(typesInputStream, "UTF-8"));
			ins.setEncoding("UTF-8");
			xmlReader.parse(ins);
			
			
			PoiCategoriesConfigurationHandler handler2 = new PoiCategoriesConfigurationHandler();
			xmlReader.setContentHandler(handler2);
			ins = new InputSource(
					new InputStreamReader(categoryToTypeInputStream, "UTF-8"));
			ins.setEncoding("UTF-8");
			xmlReader.parse(ins);
			categoryToTypeMapping = handler2.getMapping();
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d("TAG", "initialization finished");
	}

	public synchronized String resolveType(Map<String, String> tags) {
		for (TypeItem type : _nodeTypes) {
			if(type.match(tags)) {
				return type.getName();
			}
		}
		return null;
	}

	public List<TagItem> getTagging(String type) {
		for (TypeItem typeItem : _nodeTypes) {
			if(typeItem.getName().equals(type))
				return typeItem.getTagging();
		}
		return null;
	}
	
	public String getCategory(String type) {
		// TODO Very unefficient code
		String category = "unknown";
		for (Entry<String, ArrayList<String>> entry : categoryToTypeMapping.entrySet()) {
			for (String item : entry.getValue()) {
				if (item.equals(type))
					category = entry.getKey();
			}
		}
		return category;
	}
	
	public String getFullName(String typeShortName) {
		return ResourceManager.getInstance().getStringResource(typeShortName);
	}
	
	public String getCategoryFullName(String typeShortName) {
		return getFullName(getCategory(typeShortName));
	}
	
}
