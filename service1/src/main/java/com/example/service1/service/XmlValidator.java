package com.example.service1.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

@Service
public class XmlValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlValidator.class);
    private final Schema schema;
    
    public XmlValidator() throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource schemaSource = new StreamSource(new ClassPathResource("schema.xsd").getInputStream());
        this.schema = factory.newSchema(schemaSource);
    }
    
    public boolean isValid(String xmlContent) {
        try {
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlContent)));
            return true;
        } catch (SAXException e) {
            logger.warn("XML validation failed: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            logger.warn("XML parsing failed: {}", e.getMessage());
            return false;
        }
    }
} 