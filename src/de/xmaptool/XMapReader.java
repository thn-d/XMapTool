package de.xmaptool;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class XMapReader {

    /**
     * Liest XML-Input.
     * 
     * @param in
     * @return
     * @throws JAXBException
     */
    public static XuniverseMap readInputXml(String in) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("de.xmaptool");
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        XuniverseMap result = (XuniverseMap) jaxbUnmarshaller.unmarshal(new StringReader(in));
        return result;
    }
}
