/**
 *
 * Copyright 2003-2007 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.iqregister.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;

import org.jivesoftware.smackx.iqregister.packet.Registration;

import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RegistrationProvider extends IQProvider<Registration> {

    XmlPullParser parserAux;

    @Override
    public Registration parse(XmlPullParser parser, int initialDepth)
                    throws Exception {
        String instruction = null;
        Map<String, String> fields = new HashMap<>();
        List<ExtensionElement> packetExtensions = new LinkedList<>();
        parserAux =  parser;
        DataForm registrationForm = null;

        outerloop:
        while (true) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                // Any element that's in the jabber:iq:register namespace,
                // attempt to parse it if it's in the form <name>value</name>.
                if (parser.getNamespace().equals(Registration.NAMESPACE)) {
                    String name = parser.getName();
                    String value = "";

                    if (parser.next() == XmlPullParser.TEXT) {
                        value = parser.getText();
                    }
                    // Ignore instructions, but anything else should be added to the map.
                    if (!name.equals("instructions")) {
                        fields.put(name, value);
                    }
                    else {
                        instruction = value;
                    }
                }
                // Otherwise, it must be a packet extension.
                else if (parser.getNamespace().equals("jabber:x:data")) {
                    registrationForm = new DataFormProvider().parse(parser);
                    /*
                    String name = parser.getName();
                    String value = "XXXXXXX";
                    String var = "";

                    //Tag para saber en que parte del xml se esta
                    if (parser.getName().equalsIgnoreCase("field"))
                        System.out.println("### FIELD ###");

                    if (parser.getAttributeCount() > 0){
                        // Tag de if mas cantidad de atributos
                        System.out.println("~CantAtrib: " + parser.getAttributeCount());
                        for (int i=0 ; i< parser.getAttributeCount(); i++){
                            System.out.println(parser.getAttributeName(i) + ":" + parser.getAttributeValue(i));
                            if(parser.getAttributeName(i).equalsIgnoreCase("var"))
                                var = parser.getAttributeValue(i);

                            value = parser.getName();
                        }

                    }
                    if (!var.isEmpty())
                        fields.put(var, value);
                    */
                }

                else {
                    PacketParserUtils.addExtensionElement(packetExtensions, parser);
                }

            }
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(IQ.QUERY_ELEMENT)) {
                    break outerloop;
                }
            }
        }

        Registration registration = null;
        if (registrationForm != null)
        {
            registration = new Registration(instruction, fields, registrationForm);
        } else {
            registration = new Registration(instruction, fields);
        }
        registration.addExtensions(packetExtensions);
        return registration;
    }
}
