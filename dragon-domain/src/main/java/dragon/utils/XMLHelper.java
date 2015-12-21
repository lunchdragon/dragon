package dragon.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public abstract class XMLHelper {

    private XMLHelper() {
    }

    public static Document createDocument() throws ParserConfigurationException {
        return createDocument(false);
    }

    public static Document createDocument(Boolean isVaildating) throws ParserConfigurationException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(isVaildating);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }
    
    
    public static Document createDocument(String xml, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        return createDocument(new StringReader(xml), namespaceAware);
    }

    public static Document createDocument(String xml, boolean namespaceAware,boolean setColaescing) throws ParserConfigurationException, SAXException, IOException {
        return createDocument(new StringReader(xml), namespaceAware,setColaescing);
    }
    
    public static Document createDocument(File file, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        return createDocument(new FileReader(file), namespaceAware);
    }

    public static Document createDocument(Reader reader, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaceAware);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(reader));
    }
    
    public static Document createDocument(Reader reader, boolean namespaceAware,boolean setColaescing) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaceAware);
        dbf.setCoalescing(setColaescing);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new InputSource(reader));
    }
    
    public static String escapeXmlString(String value) {
        if(value!=null && (value.contains("&") || value.contains("<") || value.contains(">"))){
            value = StringUtils.replace(value, "&", "&amp;");
            value = StringUtils.replace(value, "<", "&lt;");
            value = StringUtils.replace(value, ">", "&gt;");
    //        value = StringUtils.replace(value, "'", "&apos;");
    //        value = StringUtils.replace(value, "\"", "&quot;");
            
        }
 
        return value;
    }

    public static String unescapeXmlString(String value) {
//        value = StringUtils.replace(value, "&quot;", "\"");
//        value = StringUtils.replace(value, "&apos;", "'");
        value = StringUtils.replace(value, "&gt;", ">");
        value = StringUtils.replace(value, "&lt;", "<");
        value = StringUtils.replace(value, "&amp;", "&");

        return value;
    }

    public static Node getFirstNonTextNode(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.DOCUMENT_NODE) {
                return child;
            }
        }
        return null;
    }
    
    public static Node getFirstChildNode(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0);
        }
        
        return null;
    }
    
    public static String getFirstChildTextValue(Document doc, String tagName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node child = nodeList.item(0);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)child;
                return element.getTextContent();
            } 
        }
        
        return null;
    }
    
    
    public static String getFirstChildAttributeValue(Document doc, String tagName, String attrName) {
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node child = nodeList.item(0);
            Element element = (Element)child;
            return element.getAttribute(attrName);
        }
        
        return null;
    }

    public static String getAttributeValue(Node node, String attrName) {
        NamedNodeMap attrMap = node.getAttributes();
        if (attrMap != null) {
            Node attrNode = attrMap.getNamedItem(attrName);
            if (attrNode != null) {
                return attrNode.getNodeValue();
            }
        }
        
        return null;
    }

    
    /**
     * since XPath is slow and can cause blocking issue when parallel threads try to call it, don't use this
    method to parse XML.
     * @param doc
     * @param expression
     * @return
     * @throws XPathExpressionException
     */
    @Deprecated
    public static Node evaluate(Document doc, String expression) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        return node;
    }
    
    
    /**
     * since XPath is slow and can cause blocking issue when parallel threads try to call it, don't use this
    	method to parse XML.
     * @param doc
     * @param expression
     * @return
     * @throws XPathExpressionException
     */
    @Deprecated
    public static String evaluateValue(Document doc, String expression) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String value = (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
        return value;
    }

    /**
     * Marshell specified object into XML string.
     * 
     * @return
     * @throws JAXBException
     */
    static class JAXBKey implements Serializable {
        private static final long serialVersionUID = 1961202234708193369L;
        private Class clazz[];

        public JAXBKey() {
        }

        public JAXBKey(Class[] clazz) {
            this.clazz = clazz;
        }

        public Class[] getClazz() {
            return clazz;
        }

        public void setClazz(Class[] clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JAXBKey other = (JAXBKey) obj;
            if (this.clazz != other.clazz && (this.clazz == null || !Arrays.equals(this.clazz, other.clazz))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = this.clazz != null ? this.clazz.length : 3;
            hash = 79 * hash + (this.clazz != null && this.clazz.length > 0 ? this.clazz[0].hashCode() : 0);
            return hash;
        }

    }

    final static Map<JAXBKey, JAXBContext> JAXBContextMap = new HashMap();

    private static synchronized JAXBContext getJAXBContext(Class ... clazz) throws JAXBException {
        JAXBKey key = new JAXBKey(clazz);
        JAXBContext ctx = JAXBContextMap.get(key);
        if (ctx == null) {
            ctx = JAXBContext.newInstance(clazz);
            JAXBContextMap.put(key, ctx);
        }

        return ctx;
    }

    /**
     * 
     * @param clazz
     * @param obj
     * @param size The number of char values that will fit into this buffer before it is automatically expanded
     * @return
     * @throws JAXBException
     */
    public static String marshall(Class clazz, Object obj, int size) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter(size);
        marshaller.marshal(obj, writer);
        return writer.toString();
    }
    
    public static String marshallWithoutFormat(Class clazz, Object obj, int size) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        StringWriter writer = new StringWriter(size);
        marshaller.marshal(obj, writer);
        return writer.toString();
    }

    public static String marshall(Object obj, int size, Class ... clazz) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter(size);
        marshaller.marshal(obj, writer);
        return writer.toString();
    }
    
    public static String marshallWithoutFormat(Object obj, int size, Class ... clazz) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        StringWriter writer = new StringWriter(size);
        marshaller.marshal(obj, writer);
        return writer.toString();
    }
    
    public static void marshall(Object obj, OutputStream output, Class... clazz) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(obj, output);
    }

    public static void marshall(Object obj, Node node, Class... clazz) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(obj, node);
    }

    public static Object unmarshall(Class clazz, Node node) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Unmarshaller unmarsheller = jaxbContext.createUnmarshaller();
        return unmarsheller.unmarshal(node);
    }

    public static List<Object> unmarshallList(Node root, Class clazz) throws JAXBException {
        List<Object>ret = new ArrayList<Object>();
        NodeList list = root.getChildNodes();
        for (int k = 0; k < list.getLength(); k++) {
            Node node = list.item(k);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ret.add(unmarshall(clazz, node));
            }
        }
        return ret;
    }

    public static Object unmarshall(Node node, Class ... clazz) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Unmarshaller unmarsheller = jaxbContext.createUnmarshaller();
        return unmarsheller.unmarshal(node);
    }

    public static Object unmarshall(Class clazz, Node node, XmlAdapter adapter) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(clazz);
        Unmarshaller unmarsheller = jaxbContext.createUnmarshaller();
        unmarsheller.setAdapter(adapter);
        return unmarsheller.unmarshal(node);
    }
    
    public static String toString(Node node) throws TransformerConfigurationException, TransformerException {
        return toString(node, null);
    }
    
    public static String toString(Node node, Properties oformat) throws TransformerConfigurationException, TransformerException {
        StringWriter output = new StringWriter();
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        if (oformat != null) {
            tf.setOutputProperties(oformat);
        }
        tf.transform(new DOMSource(node), new StreamResult(output));
        return output.toString();
    }
    
    public static String toIndentString(Node node) throws TransformerConfigurationException, TransformerException {
        Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "yes");
        props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return toString(node, props);
    }
    
    public static String toNoIndentString(Node node) throws TransformerConfigurationException, TransformerException {
        Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "no");
        props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        return toString(node, props);
    }

    public static String validateXmlContent(String s){
        try{
            String xml = "<root>" + s + "</root>";
            createDocument(xml, false);
            return "true";
        } catch(Exception e){
            return e.getMessage();
        }
    }

    public static String marshall(Object obj, String ... properties)throws Exception {
        if(obj == null || properties == null || properties.length < 1){
            return "";
        }

        Class clazz = obj.getClass();

        Document doc = createDocument();
        Element root = doc.createElement(StringHelper.unCapitalize(clazz.getSimpleName()));
        doc.appendChild(root);

        for(String prop : properties){
            if(StringUtils.isEmpty(prop)) continue;
            String cProp = StringUtils.capitalize(prop);
            Method getMethod = clazz.getMethod("get" + cProp);
            if(getMethod == null) continue;
            String name = prop;
            if(getMethod.isAnnotationPresent(XmlElement.class)){
                XmlElement xe = getMethod.getAnnotation(XmlElement.class);
                if(StringUtils.isNotEmpty(xe.name())){
                    name = xe.name();
                }
            }
            Object value = getMethod.invoke(obj);
            Method toString = null;
            if(value != null){
                Class vClazz = value.getClass();                
                try{
                    toString = vClazz.getMethod("toXmlString");
                } catch(Exception e){}
                if(toString != null){
                    value = toString.invoke(value);
                } else{
                    value = value.toString();
                }
            } else{
                value = "";
            }

            Node node = doc.createElement(name);
            if(toString != null){
                Document valueNode = createDocument((String)value, false);
                node = doc.importNode(valueNode.getDocumentElement(), true);
            } else{
                if(isCdata((String)value)){
                    String v = deCdata((String)value);
                    node.appendChild(doc.createCDATASection(v));
                } else{
                    node.setTextContent((String)value);
                }
            }
            root.appendChild(node);
        }

        return toIndentString(root);
    }

    public static String marshall(List objs, String ... properties)throws Exception {
        if(CollectionUtils.isEmpty(objs) || properties == null || properties.length < 1){
            return "";
        }

        Class clazz = objs.get(0).getClass();

        Document doc = createDocument();
        Element root = doc.createElement(StringHelper.unCapitalize(clazz.getSimpleName()) + "s");
        doc.appendChild(root);

        for(Object obj : objs){
            String objStr = marshall(obj, properties);
            Document objDoc = createDocument(objStr, false);
            root.appendChild(doc.importNode(objDoc.getDocumentElement(), true));
        }

        return toIndentString(root);
    }
    
    public static String transform(String xml, String xslPath) throws Exception{
        if(StringUtils.isBlank(xml) || StringUtils.isBlank(xslPath)){
            return xml;
        }
        
        //source
        Document document = createDocument(xml, false);
        DOMSource source = new DOMSource(document);
        
        //result
        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult(outWriter);
        
        //transformer
        File xsl = new File(xslPath);
        StreamSource xslSource = new StreamSource(xsl);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(xslSource);
        
        //transform
        transformer.transform(source, result);
        StringBuffer sb = outWriter.getBuffer(); 
        return sb.toString();
    }
    
    public static String deCdata(String src){
        if(isCdata(src)){
            src = src.substring("<![CDATA[".length(), src.length()-"]]>".length());
        }
        return src;
    }
    
    public static String cdata(String src){
        if(!isCdata(src)){
            return "<![CDATA[" + src + "]]>";
        }
        return src;
    }
    
    public static boolean isCdata(String src){
        if(StringUtils.isBlank(src)){
            return false;
        }
        src = src.trim().toUpperCase();
        return src.startsWith("<![CDATA[") && src.endsWith("]]>");
    }
    
	public static Map<String, Object> dom2Map(Element e) {
		Map<String, Object> map;
		
		if(e != null){
			map = new HashMap<String, Object>();
			NodeList list = e.getChildNodes();
		    int length = list.getLength();
		    if(length > 0){
		    	if(length == 1 && list.item(0).getNodeType() == Node.TEXT_NODE){
		    		Node node = list.item(0);
		    		map.put(e.getNodeName(), (node != null ? node.getNodeValue() : null));
		    	}else{
		    		int i;
			    	String name;
			    	Object value;
			    	for(i = 0; i < length; i++) {  
			    		Node node = list.item(i);
			    		if(node.getNodeType() == Node.ELEMENT_NODE){
			    			Element element = (Element)node;
			    			Map<String, Object> m = dom2Map(element);
				    		
				    		name = element.getNodeName();
				    		if(map.containsKey(name)){
				    			value = map.get(name);
				    			
				    			ArrayList<Object> values;
				    			if(value instanceof ArrayList){
				    				values = (ArrayList<Object>)value;
				    			}else{
				    				values = new ArrayList<Object>();
				    				values.add(value);
				    			}
				    			values.add(m);
				    			
				    			map.put(name, values);
				    		}else{
				    			if(m != null && m.size() == 1){
				    				String[] keys = m.keySet().toArray(new String[0]);
				    				if(keys[0].equals(name)){
				    					map.put(name, m.get(name));
				    				}else{
				    					map.put(name, m);
				    				}
				    			}else{
				    				map.put(name, m);
				    			}
				    		}
			    		}
			    	}
		    	}
		    }else{
		    	map.put(e.getNodeName(), (e != null ? e.getNodeValue() : null));
		    }
		    
		    NamedNodeMap attributeMap = e.getAttributes();
		    if(attributeMap != null){
		    	int c = attributeMap.getLength();
		    	int i;
		    	for(i = 0; i < c; i ++){
		    		Node node = attributeMap.item(i);
		    		map.put(node.getNodeName(), node.getNodeValue());
		    	}
		    }
		}else{
			map = null;
		}
		
		return map;
	}
}
