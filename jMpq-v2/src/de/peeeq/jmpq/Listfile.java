package de.peeeq.jmpq;

import java.util.LinkedList;
import java.util.Scanner;

public class Listfile {
	LinkedList<String> files = new LinkedList<String>();

	public Listfile(byte[] file){
		String list = new String(file);
		Scanner sc = new Scanner(list);
		while(sc.hasNextLine()){
			files.add(sc.nextLine());
		}
		sc.close();
		for(String s : files){
			System.out.println(s);
		}
	}
	
	public void addFile(String name){
		files.add(name);
	}
	
	public void removeFile(String name){
		files.remove(name);
	}
	
	public byte[] asByteArray(){
		String temp = "";
		for(String s : files){
			temp += s + "\n";
		}
		return temp.getBytes();
	}
}
