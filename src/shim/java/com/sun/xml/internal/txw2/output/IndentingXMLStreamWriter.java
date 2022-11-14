package com.sun.xml.internal.txw2.output;

import javax.xml.stream.XMLStreamWriter;

/*
 * Starsector uses *this* class (in *this* package) which was present in JDKs <9.
 * After JDK8 the JAXB was made into a separate library, yet the package changed,
 * hence this hack to avoid more (painful) direct bytecode editing with python,
 * since the developer is unwilling to support anything but their bundled JDK7.
 */
@SuppressWarnings("unused")
public class IndentingXMLStreamWriter extends com.sun.xml.txw2.output.IndentingXMLStreamWriter {

    public IndentingXMLStreamWriter(XMLStreamWriter arg) {
        super(arg);
    }
}
