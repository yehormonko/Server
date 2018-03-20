import Model.User;
import Model.UserList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ServerThread extends Thread {
private Socket socket;
private static UserList list;
private User user;
private static HashMap<User, Socket> userSocket;
public ServerThread(Socket socket, UserList list, HashMap<User,Socket> userSocket) {
	this.socket = socket;
	this.list=list;
	this.userSocket = userSocket;
	this.start();
}
public void run() {
	try {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
//		boolean possibility = false;
//		while (true) {
//			if (in.ready()) {
//				String first = in.readLine();
//
//				System.out.println("get from" + socket + " this string " + first);
//				boolean r = Parser(stringToXxl(first), possibility);
//				if (!r) possibility = login(stringToXxl(first));
//			}
//		}
		String login = in.readLine();
		boolean logined=false;
		if(in.ready())
		logined = login(stringToXxl(login));
		boolean cont = true;
		while (cont) {
			if (in.ready()) {
				String geted = in.readLine();
				System.out.println("get from " + socket + " this string " + geted);
				cont=Parser(stringToXxl(geted),logined);
			}
		}
	}catch (SAXException e1) {
		e1.printStackTrace();
	} catch (ParserConfigurationException e1) {
		e1.printStackTrace();
	} catch (IOException e1) {
		e1.printStackTrace();
	}
//		boolean possibility = false;
//
//		possibility = login(stringToXxl(first));
//			while (true) {
//				Parser(stringToXxl(first), possibility);
//			}
//
//	//	getString(socket);

}
public boolean Parser(Document document, boolean  possibility) {
		document.normalize();
		Element main = (Element)document.getElementsByTagName("class").item(0);
		String action = main.getAttributeNode("event").getValue();
		if(action.equals("create user")){
			Element name = (Element)main.getElementsByTagName("name").item(0);
			String username = name.getTextContent();
			Element password = (Element)main.getElementsByTagName("password").item(0);
			String userPassword = password.getTextContent();
			createUser(username,userPassword);
			notifyOnline();
			return true;
		}
	if(action.equals("logout")&&possibility){
		userSocket.remove(user);
		notifyOnline();
		return false;
	}
	if(possibility) {
		if (user.isBanned().equals("false")) {
			if (action.equals("message") && possibility) {
				Element to = (Element) main.getElementsByTagName("to").item(0);
				String receiver = to.getTextContent();
				if (userSocket.containsKey(list.getUserByName(receiver))) {
					if (list.getUserByName(receiver).isBanned().equals("false"))
						sendXML(document, userSocket.get(list.getUserByName(receiver)));
					else thisUserBanned(receiver);
				}
				return true;
			}
			if (action.equals("chat message") && possibility) {
				chatMessage(document);
				return true;
			}
			if (action.equals("get online list") && possibility) {
				onlineList(user);
				return true;
			}
			if (action.equals("change name") && possibility) {
				Element newName = (Element) main.getElementsByTagName("newname").item(0);
				String name = newName.getTextContent();
				changeName(name);
				notifyOnline();
				return true;
			}
			if (action.equals("change password") & possibility) {
				Element oldPassword = (Element) main.getElementsByTagName("password").item(0);
				String password = oldPassword.getTextContent();
				Element newPassword = (Element) main.getElementsByTagName("newpassword").item(0);
				String npassword = newPassword.getTextContent();
				changePassword(npassword, password);
				return true;
			}
			if(action.equals("ban")&&possibility&&user.isAdmin().equals("true")){
				Element user = (Element) main.getElementsByTagName("name").item(0);
				String name = user.getTextContent();
				setBan(name);
				return true;
			}
		} else {
			youAreBanned(user.getName());
			
		}
	}
	
	
	return true;
}
public static Document  stringToXxl(String string) throws ParserConfigurationException, IOException, SAXException {
	DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
	DocumentBuilder bldr = fctr.newDocumentBuilder();
	InputSource insrc = new InputSource(new StringReader(string));
	return bldr.parse(insrc);
}
public static String  XmlToString(Document doc){
	TransformerFactory tf = TransformerFactory.newInstance();
	Transformer transformer = null;
	try {
		transformer = tf.newTransformer();
	} catch (TransformerConfigurationException e) {
		e.printStackTrace();
	}
	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	StringWriter writer = new StringWriter();
	try {
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
	} catch (TransformerException e) {
		e.printStackTrace();
	}
	String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
	return output;
}
public static void  sendXML(Document document, Socket socket){
	String toSend = XmlToString(document);
	try {
		PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream())), true);
		out.println(toSend);
	} catch (IOException e) {
		e.printStackTrace();
	}
}
public boolean login(Document document){
	
	document.getDocumentElement().normalize();
	Element main = (Element)document.getElementsByTagName("class").item(0);
	String action = main.getAttributeNode("event").getValue();
	if(action.equals("login")){
	Element name = (Element)main.getElementsByTagName("name").item(0);
	String username = name.getTextContent();
	
	Element password = (Element)main.getElementsByTagName("password").item(0);
	String userPassword = password.getTextContent();
	
	
	
	
	boolean loggedIn;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document toSend = builder.newDocument();
	Element mainR = toSend.createElement("class");
	mainR.setAttribute("event","answer for login");
	toSend.appendChild(mainR);
	
		
		Element nameR = toSend.createElement("name");
		nameR.setTextContent(username);
		mainR.appendChild(nameR);
		
		Element result = toSend.createElement("result");
	if(list.check(username,userPassword)){
		userSocket.put(list.getUserByName(username),socket);
		user = list.getUserByName(username);
		result.setTextContent("true");
		loggedIn = true;
		mainR.appendChild(result);
		sendXML(toSend, socket);
		notifyOnline();
	}else{
		result.setTextContent("false");
		loggedIn = false;
		mainR.appendChild(result);
		sendXML(toSend, socket);
	}
	
	
	
	return loggedIn;
	}
	return false;
}
public boolean createUser(String username, String password){
	boolean registred;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document toSend = builder.newDocument();
	Element main = toSend.createElement("class");
	main.setAttribute("event","answer for creating user");
	toSend.appendChild(main);
	Element result = toSend.createElement("result");
	if(list.getUserByName(username)!=null){
		//create xml "username is exist"
		//break all methods
		result.setTextContent("false");
		registred = false;
	}else{
		result.setTextContent("true");
		User user = new User(username, password);
		list.addUser(user);
		registred = true;
	}
	main.appendChild(result);
	sendXML(toSend,socket);
	return registred;
}
public void chatMessage(Document document){
	for (User user:userSocket.keySet()) {
		if(!user.isBanned().equals("true"))
		sendXML(document,userSocket.get(user));
	}
}
public void onlineList(User userTo){
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document list = builder.newDocument();
	Element main = list.createElement("class");
	main.setAttribute("event","return online list");
	list.appendChild(main);
	Set<User> userList =userSocket.keySet();
	Iterator<User> iterator = userList.iterator();
	while (iterator.hasNext()) {
		User temp = iterator.next();
		Element name = list.createElement("name");
		name.setTextContent(temp.getName());
		main.appendChild(name);
		
	}
	list.normalize();
	sendXML(list,userSocket.get(userTo));
}
public void changeName(String newName){
	String res;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document document = builder.newDocument();
	Element main = document.createElement("class");
	main.setAttribute("event", "answer for changing");
	document.appendChild(main);
	if(list.getUserByName(newName)==null){
		user.setName(newName);
		list.writeFile();
		 res = "true";
	} else  res = "false";
	
	Element name = document.createElement("name");
	name.setTextContent(user.getName());
	main.appendChild(name);
	
	Element result = document.createElement("result");
		result.setTextContent(res);
		main.appendChild(result);
		
	sendXML(document,socket);
}
public void changePassword(String newPassword, String password){
	String res;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document document = builder.newDocument();
	Element main = document.createElement("class");
	main.setAttribute("event", "answer for changing");
	document.appendChild(main);
	if(user.login(password)){
		user.setPassword(newPassword);
		list.writeFile();
		res = "true";
	} else  res = "false";
	
	Element name = document.createElement("name");
	name.setTextContent(user.getName());
	main.appendChild(name);
	
	Element result = document.createElement("result");
	result.setTextContent(res);
	main.appendChild(result);
	
	sendXML(document,socket);
}
public void thisUserBanned(String name){
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document list = builder.newDocument();
	Element main = list.createElement("class");
	main.setAttribute("event","user banned");
	list.appendChild(main);
	Element username = list.createElement("name");
	username.setTextContent(name);
	main.appendChild(username);
	sendXML(list,socket);
}
public void youAreBanned(String name){
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document list = builder.newDocument();
	Element main = list.createElement("class");
	main.setAttribute("event","you are banned");
	list.appendChild(main);
	Element username = list.createElement("name");
	username.setTextContent(name);
	main.appendChild(username);
	sendXML(list,userSocket.get(this.list.getUserByName(name)));
}
public void notifyOnline(){
	Iterator<User> onl = userSocket.keySet().iterator();
	while (onl.hasNext()){
		onlineList(onl.next());
	}
}
//superuser
public void setBan(String name){
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document answer = builder.newDocument();
	Element main = answer.createElement("class");
	main.setAttribute("event","answer for banning");
	answer.appendChild(main);
	Element username = answer.createElement("name");
	username.setTextContent(name);
	main.appendChild(username);
	Element result = answer.createElement("result");
	result.setTextContent("true");
	main.appendChild(result);
	User user = list.getUserByName(name);
	if(user.isBanned().equals("true")){
		user.setBan(false);
		list.writeFile();
	}else{
		user.setBan(true);
		list.writeFile();
		youAreBanned(name);
	}
	sendXML(answer,userSocket.get(this.list.getUserByName(name)));
}
public static void youAreAdmin(String name,boolean result){
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = null;
	try {
		builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
		e.printStackTrace();
	}
	Document answer = builder.newDocument();
	Element main = answer.createElement("class");
	if(result)
	main.setAttribute("event","you are admin");
	else
	main.setAttribute("event","you are not admin");
	answer.appendChild(main);
	Element username = answer.createElement("name");
	username.setTextContent(name);
	main.appendChild(username);
	sendXML(answer,userSocket.get(list.getUserByName(name)));
}

}