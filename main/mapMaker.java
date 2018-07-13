package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public class mapMaker {

	public static void main(String[] args) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("MIDDLE", "Mid");
		map.put("TOP", "Top");
		map.put("JUNGLE", "Jungle");
		map.put("BOTTOMDUO_CARRY", "AD-Carry");
		map.put("BOTTOMDUO_SUPPORT", "Support");
		map.put("1", "Single");
		map.put("2", "Double");
		map.put("3", "Triple");
		map.put("4", "Quad");
		map.put("5", "Penta");
		
		FileOutputStream fos = new FileOutputStream("map.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(map);
		oos.close();
		System.exit(0);
		
		
	}

}
