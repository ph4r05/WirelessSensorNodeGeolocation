/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.nodeRegister;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Save and restore node register configuration
 * 
 * @author ph4r05
 */
public class NodeRegisterConfigurationManager {
    
    public static XStream getXstream(){
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("SimpleGenericNode", SimpleGenericNode.class);
        xstream.alias("GenericPlatform", rssi_graph.nodeRegister.NodePlatformGeneric.class);
        xstream.alias("TelosBPlatform", rssi_graph.nodeRegister.NodePlatformTelosb.class);
        xstream.alias("IRISPlatform", rssi_graph.nodeRegister.NodePlatformIris.class);
        xstream.alias("LogNormalShadowing", rssi_graph.rssi.RSSI2DistLogNormalShadowing.class);
        xstream.processAnnotations(MobileNode.class);
        return xstream;
    }
    
    /**
     * Used for node register serialization. Some attributes are serialized
     * and writen to XML.
     * 
     * @return 
     */
    public static int storeConfigToXML(String file, 
            NodeRegisterConfiguration config) throws IOException{
        
        // serialize config object to ostream
        XStream xstream = NodeRegisterConfigurationManager.getXstream();
        String xml = xstream.toXML(config);
        
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(file));
        outputStream.write(xml);
        outputStream.flush();
        outputStream.close();
        
        return 0;
    }
    
    /**
     * Reads XML configuration and tries to restore it.
     * 
     * @return 
     */
    public static NodeRegisterConfiguration getConfigFromXML(String file) throws FileNotFoundException, IOException, NodeRegisterConfigurationException{
        
        String xml=null;
        FileInputStream stream = new FileInputStream(new File(file));
        
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            
            /* Instead of using default, pass in a decoder. */
            xml = Charset.defaultCharset().decode(bb).toString();
        }
        finally {
            stream.close();
        }
        
        // empty config string?
        if (xml==null || xml.isEmpty()) throw new NodeRegisterConfigurationException("Cannot read file, XML is empty");
        
        XStream xstream = NodeRegisterConfigurationManager.getXstream();
        NodeRegisterConfiguration newConfig = (NodeRegisterConfiguration)xstream.fromXML(xml);
        
        // null config object
        if (newConfig==null)  throw new NodeRegisterConfigurationException("Cannot read configuration, config object is empty");
        
        return newConfig;
    }
}
