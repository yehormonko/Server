import Model.User;
import Model.UserList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class Server {
	private static UserList list = new UserList();
	private static HashMap<User,Socket> users= new HashMap<>();
	public Server(){
		try {
			ServerSocket socketListener = new ServerSocket(7777);
			while (true) {
				if(!socketListener.isBound())
				System.out.println("+");
				Socket socket = socketListener.accept();
				new ServerThread(socket,list,users);
			}
		} catch (SocketException e) {
			System.err.println("Socket exception");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O exception");
			e.printStackTrace();
		}
	}
	public static void commands(){
		String help = "#/help";
		String admin = "#/admin username y/n";
		Scanner scanner = new Scanner(System.in);
		String command = scanner.nextLine().trim();
		if(command.equals("/help")){
			System.out.println(help+", "+admin+", /online");
		}else if(command.startsWith("/admin")) {
			String name = command.substring("/admin ".length(), command.length() - 1).trim();
			if (list.getUserByName(name) != null) {
				boolean result = false;
				if (command.endsWith("y")) {
					list.getUserByName(name).setAdmin(true);
					result = true;
					System.out.println("#"+name+" is admin now");
				} else if (command.endsWith("n") && list.getUserByName(name).isAdmin().equals("true")) {
					list.getUserByName(name).setAdmin(false);
					result = false;
					System.out.println("#"+name+"is not admin anymore");
				} else System.out.println("#wrong format");
				list.writeFile();
				if (users.containsKey(list.getUserByName(name)))
					ServerThread.youAreAdmin(name, result);
			} else System.out.println("#there is no such user");
		}else if(command.startsWith("/online")){
			Iterator<User> iterator = users.keySet().iterator();
			while (iterator.hasNext()){
				System.out.println("    "+iterator.next().getName());
			}
		}
		else System.out.println("#there is no such command");
		
	}
public static void main(String[] args) {
		Thread commands = new Thread(){
			@Override
			public void run(){
				System.out.println("#print /help to see all server commands");
				while (true){
					System.out.print("#");
					commands();
				}
			}
	};
		commands.start();
		System.out.println("server started");
	Server server = new Server();
}
}
