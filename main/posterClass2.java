package main;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.match.dto.Match;
import net.rithms.riot.api.endpoints.match.dto.Participant;
import net.rithms.riot.api.endpoints.match.dto.ParticipantStats;
import net.rithms.riot.api.endpoints.match.dto.ParticipantTimeline;
import net.rithms.riot.api.endpoints.match.dto.TeamStats;
import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.Image;
import net.rithms.riot.constant.Platform;

public class posterClass2 {
	net.rithms.riot.api.endpoints.static_data.dto.ChampionList championList;
	HashMap<String, String> map;
	final String artUrl = "http://ddragon.leagueoflegends.com/cdn/8.13.1/img/champion/";
	final String splashUrl = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/";

	@SuppressWarnings("unchecked")
	public posterClass2() throws IOException, ClassNotFoundException {
		//Use the cached version of ChampionList
		
		FileInputStream fis = new FileInputStream("ChampList.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		championList= (net.rithms.riot.api.endpoints.static_data.dto.ChampionList) ois.readObject();
		fis = new FileInputStream("map.ser");
		ois = new ObjectInputStream(fis);
		map = (HashMap<String, String>) ois.readObject();
		ois.close();
	}

	public posterClass2(RiotApi api) throws RiotApiException, IOException {
		// Update the cached ChampionList
		championList = api.getDataChampionList(Platform.EUW, null, null, true, ChampionListTags.ALL);

		FileOutputStream fos = new FileOutputStream("ChampList.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(championList);
		oos.close();
		System.exit(0);

	}

	public void postMatchResult(Match finalMatch, int myID, String myName, boolean niceness)
			throws UnsupportedEncodingException, RiotApiException {
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		Participant subject = finalMatch.getParticipantBySummonerId(myID);
		if(niceness && (subject.getStats().getKills() < subject.getStats().getDeaths())) return;
		Champion ch = championList.getData().get(String.valueOf(subject.getChampionId()));
		
		
		String JsonString = makeJsonPost(myName, ch, subject,finalMatch);
		System.out.println(JsonString);
		StringEntity entity = new StringEntity(JsonString);
		postFunction(httpClient, entity);

	}

	public String makeJsonPost(String myName, Champion ch, Participant subject,Match match) {
		StringBuilder strBld = new StringBuilder();
		strBld = jsonBlock(strBld, myName, ch, subject,match);
		return strBld.toString();

	}

	public StringBuilder jsonBlock(StringBuilder strBld, String myName, Champion ch, Participant subject,Match match) {
		strBld.append("{");
		strBld = addPrefix(strBld, "content", myName);
		strBld.append(",");
		strBld = startEmbededblock(strBld, ch, subject,match);
		strBld.append("}");
		return strBld;
	}

	@SuppressWarnings("unused")
	public StringBuilder startEmbededblock(StringBuilder strBld, Champion ch, Participant subject,Match match) {
		ParticipantStats stats = subject.getStats();
		TeamStats myTeam = match.getTeamByTeamId(subject.getTeamId());
		
		ParticipantTimeline timeline = subject.getTimeline();
		strBld.append("\"embeds\":[{");
		System.out.println(ch.getImage());
		strBld.append(jsonAuthor(ch.getName(), ch.getImage()));
		strBld.append(",");
		strBld.append("\"fields\":[");
		strBld.append(jsonField("K/D/A", stats.getKills() + "/" + stats.getDeaths() + "/" + stats.getAssists()));
		strBld.append(",");
		String str;
		if (stats.isWin()) {
			str = "Win";
		} else {
			str = "Loss";
		}
		strBld.append(jsonField("Win/Loss", str));
		strBld.append(",");
		
		String lane = timeline.getLane();
		System.out.println(lane + ": pre fix");
		if(lane.equals("BOTTOM")) lane+=timeline.getRole();
		if(lane == null) {
			strBld.append(jsonField("Gamemode", "URF"));
		}else {
			strBld.append(jsonField("Lane", map.get(lane)));
			lane = map.get(lane);
			System.out.println(lane +": past fix");
		strBld.append(",");
		switch(lane) {
			 
			case "Support": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getStatFor("Total Healing", stats.getTotalHeal()));
				strBld.append(",");
				strBld.append(getStatFor("Wards Placed", stats.getWardsPlaced()));
				strBld.append(",");
				strBld.append(getStatFor("Wards Removed", stats.getWardsKilled()));
				
				
				break;
			case "Jungle": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getStatFor("Best Multikill", map.get(String.valueOf(stats.getLargestMultiKill()))));
				strBld.append(",");
				strBld.append(getStatFor("Team Jungle Creeps", stats.getNeutralMinionsKilledTeamJungle()));
				strBld.append(",");
				strBld.append(getStatFor("Enemy Jungle Creeps", stats.getNeutralMinionsKilledEnemyJungle()));
				strBld.append(",");
				strBld.append(getStatFor("Damage to Champions", stats.getTotalDamageDealtToChampions()));
				strBld.append(",");
				strBld.append(getStatFor("Baron kills",myTeam.getBaronKills()));
				strBld.append(",");
				strBld.append(getStatFor("Dragon kills",myTeam.getDragonKills()));
				
				
				break;
			case "AD-Carry":
			case "Mid": 
			case "Top": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getStatFor("Best Multikill", map.get(String.valueOf(stats.getLargestMultiKill()))));
				strBld.append(",");
				strBld.append(getStatFor("Minions killed", stats.getTotalMinionsKilled()));
				strBld.append(",");
				strBld.append(getStatFor("Damage to Champions", stats.getTotalDamageDealtToChampions()));
				break;
		}}
		
		
		
		
		strBld.append("]");
		strBld.append(",");
		strBld.append(jsonBlock("image", "url", splashUrl+ch.getImage().getFull().replace(".png", "_0.jpg")));
		strBld.append("}]");
		return strBld;
	}

	private Object getStatFor(String name, String string) {	
		return jsonField(name,string);
	}

	private String getStatFor(String name, long value) {
		return jsonField(name, String.valueOf(value));
	}

	private String getGameDuration(Match match) {
		return jsonField("Game Duration", dateThis(match.getGameDuration()));
	}

	private String dateThis(long gameDuration) {
		int min= (int) (gameDuration/60);
		int sec = (int) (gameDuration%60);
		if (sec/10 == 0) return min+":0"+sec;
		return min+":"+sec;
	}

	public StringBuilder addPrefix(StringBuilder strBld, String string, String myName) {
		return strBld.append(jsonThis(string, myName + " just finished a game"));

	}

	public String jsonField(String s1, String s2) {
		return "{" + jsonThis("name", s1) + "," + jsonThis("value", s2) + "," + jsonThis("inline", "true") + "}";
	}

	public String jsonAuthor(String string, Image image) {
		
		return "\"author\":{" + jsonThis("name", string)+"," +jsonThis("icon_url", artUrl+image.getFull()) + "}";// ",";
	}
	public String jsonBlock(String header, String key, String value) {
		
		return "\""+header+"\":{" + jsonThis(key, value)+"}";// ",";
	}

	public void postFunction(HttpClient httpClient2, StringEntity entity) {
		try {

			HttpPost httpPost = new HttpPost(
					"https://discordapp.com/api/webhooks/465632157164109834/QK-XgBgaO9YEAcf-FqqvTZkbsaZBvjyQjRyJh2T8mysnFYh-da_p_hxQKNREbtelyCkv");

			httpPost.setEntity(entity);

			httpPost.setHeader("Content-type", "application/json");

			 @SuppressWarnings("unused")
			 HttpResponse response = httpClient2.execute(httpPost);

		} catch (Exception ex) {
		}
	}

	public String jsonThis(String key, String value) {
		return "\"" + key + "\":\"" + value + "\"";
	}

}
