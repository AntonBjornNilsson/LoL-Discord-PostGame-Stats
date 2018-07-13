package main;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.constant.Platform;

import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class pollingThread extends Thread {
	RiotApi api;
	String myName;
	RiotBot parent;
	HttpClient httpClient;
	int myID;
	CurrentGameInfo lastgame;
	boolean niceness;

	public pollingThread(RiotApi api, String friendsnames, int myID, RiotBot parent, boolean niceness) {
		this.api = api;
		myName = friendsnames;
		this.parent = parent;
		this.myID = myID;
		httpClient = HttpClientBuilder.create().build();
		this.niceness=niceness;
	}

	@Override
	public void run() {
		
		firstRun();
		while (true) {
			try {
				api.getActiveGameBySummoner(Platform.EUW, myID);
				
				System.out.println(myName + " is playing game: "+lastgame.getGameId()+"\n");
				Thread.sleep(10000);
			} catch (RiotApiException e) {
				System.out.println(myName + " has finished a game");
				
				long gameID;
				try {
					gameID = lastgame.getGameId();
					System.out.println("The system is sleeping");
					Thread.sleep(30000);
				} catch (NullPointerException | InterruptedException e2){
					return;
				}
				
				try {
					Match finalMatch = api.getMatch(Platform.EUW, gameID, myID);
					posterClass pC = new posterClass();
					pC.postMatchResult(finalMatch, myID, myName,niceness);
					
					Thread.sleep(50000);
					parent.setInactive(myName);
					return;
				} catch (RiotApiException | ClassNotFoundException | IOException | InterruptedException e1) {
					System.out.println("404 prob");
					parent.setInactive(myName);
					return;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void firstRun() {
		try {
			lastgame = api.getActiveGameBySummoner(Platform.EUW, myID);
		} catch (RiotApiException e) {
			return;
			
		}
	}

}
