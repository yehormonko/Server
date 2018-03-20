package Client;

import Model.User;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SimpleClient {
public static void main(String[] args) throws IOException {
	// Передаем null в getByName(), получая
	// специальный IP адрес "локальной заглушки"
	// для тестирования на машине без сети:
	InetAddress addr = InetAddress.getByName(null);
	// Альтернативно, вы можете использовать
	// адрес или имя:
	// InetAddress addr =
	// InetAddress.getByName("127.0.0.1");
	// InetAddress addr =
	// InetAddress.getByName("localhost");
	System.out.println("addr = " + addr);
	Socket socket = new Socket(addr, 7777);
	// Помещаем все в блок try-finally, чтобы
	// быть уверенным, что сокет закроется:
	
	System.out.println("socket = " + socket);
	BufferedReader in = new BufferedReader(new InputStreamReader(socket
			.getInputStream()));
	// Вывод автоматически Output быталкивается PrintWriter'ом.
	PrintWriter out = new PrintWriter(new BufferedWriter(
			new OutputStreamWriter(socket.getOutputStream())), true);
//		for (int i = 0; i < 10; i++) {
//			out.println("howdy " + i);
//			String str = in.readLine();
//			System.out.println(str);
//		}
//		out.println("END");
	//out.println(xmlToStr(new Message("text",new User("name", "other name","pass")).makeXML()));
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
			"<class event=\"login\">" +
			"<name>changed name2</name>" +
			"<password>pass</password" +
			"></class>");
	//while (true){
	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
			"<class event=\"chat message\">" +
			"<from>changed name2</from>" +
			"<text>changed name2</text>" +
			"</class>");
	
	while (true) in.ready();
	//System.out.println(in.readLine());}
	//}


//public static String xmlToStr(Document doc){
//	TransformerFactory tf = TransformerFactory.newInstance();
//	Transformer transformer = null;
//	try {
//		transformer = tf.newTransformer();
//	} catch (TransformerConfigurationException e) {
//		e.printStackTrace();
//	}
//	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//	StringWriter writer = new StringWriter();
//	try {
//		transformer.transform(new DOMSource(doc), new StreamResult(writer));
//	} catch (TransformerException e) {
//		e.printStackTrace();
//	}
//	String output1 = writer.getBuffer().toString().replaceAll("\n|\r", "");
//	return output1;
//}
}
}