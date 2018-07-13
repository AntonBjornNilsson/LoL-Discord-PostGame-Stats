package main;

import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class RiotBot implements Runnable {

	final static String[] friendsNames = { "friend1", "friend", "friend", "friend"};
	final static int[] friendsID = { 0, 1, 2, 3};
	static boolean[] isActive = { false, false, false, false};
	static boolean[] niceArray = { false, false, false, false};
	String yourDevKey = "INSERT-API-KEY-HERE";

	public RiotApi setup() {
		ApiConfig config = new ApiConfig().setKey(yourDevKey);
		return new RiotApi(config);
	}

	private void printCurrent(RiotApi api, String name) {
		System.out.println("----------------------------------------------------------------");
		try {
			printNameByString(api, name);
		} catch (RiotApiException e1) {
			e1.printStackTrace();
		}

	}

	private static void printNameByString(RiotApi api, String summonerName) throws RiotApiException {
		Summoner summoner = api.getSummonerByName(Platform.EUW, summonerName);
		System.out.println("Name: " + summoner.getName());
		System.out.println("Summoner ID: " + summoner.getId());
		System.out.println("Account ID: " + summoner.getAccountId());
		System.out.println("Summoner Level: " + summoner.getSummonerLevel());
		System.out.println("Profile Icon ID: " + summoner.getProfileIconId());
	}

	public void setInactive(String myName) {
		for (int i = 0; i < friendsNames.length; i++) {
			if (friendsNames[i].equals(myName))
				isActive[i] = false;
		}
	}

	@Override
	public void run() {
		RiotApi api = setup();
		
		while (true) {
			for (int boner = 0; boner < friendsNames.length; boner++) {

				if (!isActive[boner]) {

					printCurrent(api, friendsNames[boner]);
					try {
						CurrentGameInfo cGame = api.getActiveGameBySummoner(Platform.EUW, friendsID[boner]);
						System.out.println(friendsNames[boner] + " is playing game " + cGame.getGameId());
						System.out.println("----------------------------------------------------------------");
						Thread poller = new pollingThread(api, friendsNames[boner], friendsID[boner], this, niceArray[boner]);
						poller.start();
						isActive[boner] = true;
					} catch (RiotApiException e) {
						System.out.println("Player is not in a game");
						System.out.println("----------------------------------------------------------------");
					}

				}
				try {
				Thread.sleep((long) (1000));
					} catch (InterruptedException e) {
				e.printStackTrace();
				}
			}
			try {
				Thread.sleep((long) (500000 + Math.random() * 10000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
