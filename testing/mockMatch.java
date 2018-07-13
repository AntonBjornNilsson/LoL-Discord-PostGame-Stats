package testing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.constant.Platform;

public class mockMatch implements Runnable {
	final static String[] friendsNames = { "Friend1", "Friend2", "Friend3", "Friend4", "Friend5" };
	final static int[] friendsID = { 000001, 000002, 000003, 000004, 000005 };
	String yourDevKey="InsertApiKeyHere";
	String newOrCached = "new";
	
	@Override
	public void run() {

		try {

			experimentalPosterClass pC = new experimentalPosterClass();
			long game = 0L;// YourGameHere

			ApiConfig config = new ApiConfig().setKey(yourDevKey);
			RiotApi api = new RiotApi(config);
			Match finalMatch = api.getMatch(Platform.EUW, game, friendsID[0]);

			if (newOrCached.equals("new")) {
				// write object to file
				FileOutputStream fos = new FileOutputStream("thatMatch.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(finalMatch);
				oos.close();
				System.exit(0);
			}
			if (newOrCached.equals("cached")) {
				// read object from file
				FileInputStream fis = new FileInputStream("thatMatch.ser");
				ObjectInputStream ois = new ObjectInputStream(fis);
				Match result = (Match) ois.readObject();
				ois.close();
			
			pC.postMatchResult(result, friendsID[0], friendsNames[0], false);
			}

		} catch (

		FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
