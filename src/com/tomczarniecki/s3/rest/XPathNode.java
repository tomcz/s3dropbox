/*
 * Copyright (c) 2010, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tomczarniecki.s3.rest;

import com.tomczarniecki.s3.Lists;
import com.tomczarniecki.s3.Maps;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XPathNode {

    private final Node node;
    private final XPath xpath;

    public XPathNode(InputStream input) {
        this.xpath = createXPath("aws", "http://s3.amazonaws.com/doc/2006-03-01/");
        this.node = parseDocument(input);
    }

    private XPathNode(Node node, XPath xpath) {
        this.node = node;
        this.xpath = xpath;
    }

    public String queryForText(String expression) {
        try {
            return (String) xpath.evaluate("string(" + expression + ")", node, XPathConstants.STRING);

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public List<XPathNode> queryForNodes(String expression) {
        try {
            NodeList nodes = (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
            List<XPathNode> result = Lists.createWithSize(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(new XPathNode(nodes.item(i), xpath));
            }
            return result;

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", 2);

            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Source source = new DOMSource(node);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            transformer.transform(source, result);

            return writer.toString();

        } catch (TransformerException e) {
            return node.toString();
        }
    }

    private static XPath createXPath(String alias, String namespaceURI) {
        NamespaceContext context = new XPathNamespaceContext(alias, namespaceURI);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(context);
        return xpath;
    }

    private static Document parseDocument(InputStream input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(input);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);

        } catch (SAXException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    private static class XPathNamespaceContext implements NamespaceContext {

        private Map<String, String> prefixToNamespaceUri = Maps.create();
        private Map<String, List<String>> namespaceUriToPrefixes = Maps.create();

        public XPathNamespaceContext(String prefix, String namespaceURI) {
            bindNamespaceUri(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            bindNamespaceUri(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
            bindNamespaceUri(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
            bindNamespaceUri(prefix, namespaceURI);
        }

        public String getNamespaceURI(String prefix) {
            String uri = prefixToNamespaceUri.get(prefix);
            return (uri != null) ? uri : XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceUri) {
            List prefixes = prefixesFor(namespaceUri);
            return prefixes.isEmpty() ? null : (String) prefixes.get(0);
        }

        public Iterator getPrefixes(String namespaceUri) {
            return prefixesFor(namespaceUri).iterator();
        }

        private void bindNamespaceUri(String prefix, String namespaceUri) {
            prefixToNamespaceUri.put(prefix, namespaceUri);
            prefixesFor(namespaceUri).add(prefix);
        }

        private List<String> prefixesFor(String namespaceUri) {
            List<String> list = namespaceUriToPrefixes.get(namespaceUri);
            if (list == null) {
                list = Lists.create();
                namespaceUriToPrefixes.put(namespaceUri, list);
            }
            return list;
        }
    }
}
