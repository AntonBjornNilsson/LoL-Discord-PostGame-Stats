package testing;
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
import net.rithms.riot.api.endpoints.match.dto.ParticipantIdentity;
import net.rithms.riot.api.endpoints.match.dto.ParticipantStats;
import net.rithms.riot.api.endpoints.match.dto.ParticipantTimeline;
import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.Image;
import net.rithms.riot.constant.Platform;

public class experimentalPosterClass {
	net.rithms.riot.api.endpoints.static_data.dto.ChampionList championList;
	HashMap<String, String> map;
	final String artUrl = "http://ddragon.leagueoflegends.com/cdn/8.13.1/img/champion/";
	final String splashUrl = "http://ddragon.leagueoflegends.com/cdn/img/champion/splash/";

	@SuppressWarnings("unchecked")
	public experimentalPosterClass() throws IOException, ClassNotFoundException {
		//Use the cached version of ChampionList
		
		FileInputStream fis = new FileInputStream("ChampList.ser");
		ObjectInputStream ois = new ObjectInputStream(fis);
		championList= (net.rithms.riot.api.endpoints.static_data.dto.ChampionList) ois.readObject();
		fis = new FileInputStream("map.ser");
		ois = new ObjectInputStream(fis);
		map = (HashMap<String, String>) ois.readObject();
		ois.close();
	}

	public experimentalPosterClass(RiotApi api) throws RiotApiException, IOException {
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

	public StringBuilder startEmbededblock(StringBuilder strBld, Champion ch, Participant subject,Match match) {
		ParticipantStats stats = subject.getStats();
		ParticipantTimeline timeline = subject.getTimeline();
		strBld.append("\"embeds\":[{");
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
		if(lane.equals("BOTTOM")) lane+=timeline.getRole();
		if(lane.equals("null")) {
			strBld.append(jsonField("Lane", "URF"));
		}else {
			strBld.append(jsonField("Lane", map.get(lane)));
			lane = map.get(lane);
		}
		strBld.append(",");
		switch(lane) {
			case "AD-Carry": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getHighestMultikill(stats));
				strBld.append(",");
				strBld.append(getMinionsKilled(stats));
				strBld.append(",");
				strBld.append(getChampDamage(stats));
				
				
				break;
			case "Support": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getHeals(stats));
				//wards
				//wards removed
				
				break;
			case "Jungle": 
				strBld.append(getGameDuration(match));
				strBld.append(",");
				strBld.append(getHighestMultikill(stats));
				strBld.append(",");
				strBld.append(getMinionsKilled(stats));
				strBld.append(",");
				strBld.append(getChampDamage(stats));
				//dragons
				
				break;
			case "Mid": 
			case "Top": break;
		}
		
		
		
		
		strBld.append("]");
		strBld.append(",");
		strBld.append(jsonBlock("image", "url", splashUrl+ch.getImage().getFull().replace(".png", "_0.jpg")));
		strBld.append("}]");
		return strBld;
	}

	private String getHeals(ParticipantStats stats) {
		return jsonField("Total Healing", String.valueOf(stats.getTotalHeal()));
	}

	private String getGameDuration(Match match) {
		return jsonField("Game Duration", dateThis(match.getGameDuration()));
	}

	private String dateThis(long gameDuration) {
		int min= (int) (gameDuration/60);
		int sec = (int) (gameDuration%60);
		return min+":"+sec;
	}

	private String getMinionsKilled(ParticipantStats stats) {
		
		return jsonField("Minions killed", String.valueOf(stats.getNeutralMinionsKilled()));
	}

	private String getHighestMultikill(ParticipantStats stats) {
		
		return jsonField("Best Multikill", String.valueOf(stats.getLargestMultiKill()));
	}

	private String getChampDamage(ParticipantStats stats) {
		return jsonField("Damage to Champions", String.valueOf(stats.getTotalDamageDealtToChampions()));
		
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
					"");

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
