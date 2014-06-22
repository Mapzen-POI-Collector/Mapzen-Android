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

package com.mapzen.io;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.configuration.TagItem;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.osm.Changeset;
import com.mapzen.data.osm.OsmNode;

/**
 * Save the dataset into a stream as osm intern xml format. This is not using any
 * xml library for storing.
 * @author imi
 */
public class OsmWriter extends XmlWriter implements MapzenConstants {

    private boolean osmConform;
    private Changeset changeset;

    public OsmWriter(PrintWriter out) {
        super(out);
    }

    public void setChangeset(Changeset cs) {
        this.changeset = cs;
    }

    public void header() {
        out.println("<?xml version='1.0' encoding='UTF-8'?>");
        out.print("<osm version='");
        out.print(OSM_API_VERSION);
        out.print("' generator='");
        out.print(OSM_CREATOR_INFO);
        out.println("'>");
    }
    public void footer() {
        out.println("</osm>");
    }

    public void osmChangeHeader() {
        out.println("<?xml version='1.0' encoding='UTF-8'?>");
        out.print("<osmChange version='");
        out.print(OSM_API_VERSION);
        out.print("' generator='");
        out.print(OSM_CREATOR_INFO);
        out.println("'>");
    }

    public void osmChangeActionOpen(String action) {
        out.println("  <"+action+">");
    }

    public void osmChangeActionClose(String action) {
        out.println("  </"+action+">");
    }

    public void osmChangeFooter() {
        out.println("</osmChange>");
    }


    public void visit(OsmNode node) {
        out.println("<node");
        addAttributes(node);
        out.print(">");
        addTags(node);
        out.println("</node>");
    }

    public void visit(Changeset cs) {
        out.println("  <changeset>");
        out.print("    <tag k='created_by' v='");
        out.print(OSM_CREATOR_INFO);
        out.println("' />");
        out.println("  </changeset>");
    }


    private static final Comparator<Entry<String, String>> byKeyComparator = new Comparator<Entry<String,String>>() {
        public int compare(Entry<String, String> o1, Entry<String, String> o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    };

    private void addTags(OsmNode node) {
        String name = node.getName();
        String addr_h = node.get_addr_housenumber();
        String addr_s = node.get_addr_street();
        String website = node.get_website();
        String phone = node.get_phone();
        String open_hours = node.get_opening_hours();
        String description = node.get_description();
        String cmTypeName = node.getType();
        List<TagItem> cmTypeTags = OsmDriver.getInstance().getTagging(cmTypeName);

        if (name != null)
            out.println("    <tag k='name' v='"+XmlWriter.encode(name)+ "' />");
        if (addr_h != null)
            out.println("    <tag k='addr:housenumber' v='"+XmlWriter.encode(addr_h)+ "' />");
        if (addr_s != null)
            out.println("    <tag k='addr:street' v='"+XmlWriter.encode(addr_s)+ "' />");
        if (website != null)
            out.println("    <tag k='website' v='"+XmlWriter.encode(website)+ "' />");
        if (phone != null)
            out.println("    <tag k='phone' v='"+XmlWriter.encode(phone)+ "' />");
        if (open_hours != null)
            out.println("    <tag k='opening_hours' v='"+XmlWriter.encode(open_hours)+ "' />");
        if (description != null)
            out.println("    <tag k='description' v='"+XmlWriter.encode(description)+ "' />");
        for (TagItem tag : cmTypeTags) {
            out.println("    <tag k='"+XmlWriter.encode(tag.getKey())+"' v='"+XmlWriter.encode(tag.getValue())+ "' />");
        }

        if (node.hasTags()) {
            List<Entry<String, String>> entries = new ArrayList<Entry<String,String>>(node.getTags().entrySet());
            Collections.sort(entries, byKeyComparator);
            for (Entry<String, String> e : entries) {
                if (!("created_by".equals(e.getKey()))) {
                    out.println("    <tag k='"+ XmlWriter.encode(e.getKey()) +
                            "' v='"+XmlWriter.encode(e.getValue())+ "' />");
                }
            }
        }

    }

    /**
     * Add the common part as the form of the tag as well as the XML attributes
     * id, action, user, and visible.
     */
    private void addAttributes(OsmNode node) {
        if (node.getId() != 0) {
            out.print(" id='"+ node.getId()+"'");
        } else
            throw new IllegalStateException(String.format("Unexpected id 0 for osm primitive found"));

        if (node.getVersion() != 0) {
            out.print(" version='"+node.getVersion()+"'");
        }

        if (this.changeset != null && this.changeset.getId() != 0) {
            out.print(" changeset='"+this.changeset.getId()+"'" );
        }

        out.print(" lat='"+node.getCoordinates().getLatitudeE6()/1.E6+"' lon='"+node.getCoordinates().getLongitudeE6()/1.E6+"'");

        Map<String, String> otherAttributes = node.getAttributes();
        for(Entry<String, String> keyValuePair : otherAttributes.entrySet()) {
            out.print(" "+keyValuePair.getKey()+"='"+keyValuePair.getValue()+"'");
        }
    }

    public void close() {
        out.close();
    }

    public void flush() {
        out.flush();
    }


}
