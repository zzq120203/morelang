package cn.ac.iie.Util;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.DicAnalysis;

import cn.ac.iie.entity.HotEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * deal wechat message, if it is a hot, then get a HotMsg, else null
 */
public class DealEvent {
	private static MapUtil maps;

	public DealEvent() {
		maps = MapUtil.getInstance();
	}

	/**
	 * judge whether this message is a hot issue
	 * 
	 * @param message
	 * @return a HotMsg object if this message is a hot issue else return null
	 */
	public HotEvent filterHotEvent(String message) {
		if (this.containsFilterOutWord(message) || message.length() > 200 || message.length() < 5)
			return null;
		String[] sentences = this.cutSentence(message);
		if (sentences == null)
			return null;
		int length = sentences.length;

		for (int i = 0; i < length; i++) {
			Result result = DicAnalysis.parse(sentences[i]);
			List<String> locAndEvtList = this.haveLocationAndEvent(result);
			if (!this.containsFilterOutWord(sentences[i]) && locAndEvtList != null && locAndEvtList.size() == 3
					&& !this.isQuestion(sentences[i])) {
				HotEvent hotMsg = new HotEvent();

				// 消息对应关键词
				List<String> keywordList = new ArrayList<String>();
				String locationKeyword = locAndEvtList.get(2);
				String event = locAndEvtList.get(1);
				keywordList.add(locationKeyword);
				keywordList.add(event);
				hotMsg.setKeyword(keywordList);
				
				hotMsg.setKw_locale(locationKeyword);
				hotMsg.setKw_event(event);
				
				// 追溯上级地点和事件
				String location=locAndEvtList.get(0);
				String upperLocation = maps.trackLocation(location);
				if (upperLocation == null) {
					hotMsg.setE_province(locAndEvtList.get(0));
				} else {
					String[] uppers = upperLocation.split(",");
					if (uppers.length == 1) {
						hotMsg.setE_province(uppers[0]);
						hotMsg.setE_city(location);
					} else {
						hotMsg.setE_province(uppers[1]);
						hotMsg.setE_city(uppers[0]);
					}
				}
				String eventClass = maps.trackEvent(event);
				hotMsg.setE_name(event);
				hotMsg.setE_class(eventClass);
				return hotMsg;
			}
		}
		return null;
	}

	/**
	 * get the location and event keyword for a message
	 *
	 * update: add the limit for the number of noun in a sentence
	 * 
	 * @param result:segmentation
	 *            of a message
	 * @return true if have event and location keyword else return false
	 */
	public List<String> haveLocationAndEvent(Result result) {
		List<Term> termList = result.getTerms();
		String locKeyword = null;
		String evtKeyword = null;
		String trueLocation = null;
		int nounCount = 0;
		for (Term term : termList) {
			maps.updateHotWordMap(term.getName());
			if (term.getNatureStr().equals("location") || term.getNatureStr().equals("ns")) {
				locKeyword = term.getName();
				// 保留消息中实际的地名
				trueLocation = locKeyword;
				// 在map中进行地点同名转换
				locKeyword = maps.locationConvert(locKeyword);
			}
			if (term.getNatureStr().equals("event")) {
				evtKeyword = term.getName();
			}
			if (term.getNatureStr().startsWith("n")||term.getNatureStr().startsWith("v"))
				nounCount++;
		}
		if (locKeyword != null && evtKeyword != null && nounCount >= 1) {
			List<String> resultList = new ArrayList<String>();
			resultList.add(locKeyword);
			resultList.add(evtKeyword);
			resultList.add(trueLocation);
			return resultList;
		}
		return null;
	}

/**
 * judge whether sentence contains filter out word
 * @param sentence
 * @return: return true if it contains... else return false
 */
	public boolean containsFilterOutWord(String sentence) {
		Set<String> wordSet = maps.getFilterOutWord();
		for (String word : wordSet) {
			String[] words = word.split(",");
			boolean flag = true;
			for (String sword : words) {
				if (!sentence.contains(sword)) {
					flag = false;
					break;
				}
			}
			if (flag)
				return true;
		}

		return false;
	}

	/**
	 * determine whether this sentence is a question or not
	 * 
	 * @param sentence
	 * @return
	 */
	public boolean isQuestion(String sentence) {
		if (sentence.contains("?") || sentence.contains("？") || sentence.contains("吗") || sentence.contains("哪")
				|| sentence.contains("怎么") || sentence.contains("什么")) {
			return true;
		}
		return false;
	}

	/**
	 * compared with the previous version, this version can reserve punctuation cut
	 * a message to sentences
	 * 
	 * @param message:
	 *            a text which may contains many sentences
	 * @return:[]String
	 */
	public String[] cutSentence(String message) {
		String regEx = "[？。！；!?!;]";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(message);
		String[] sentences = pattern.split(message);
		if (sentences.length > 0) {
			int count = 0;
			while (count < sentences.length) {
				if (matcher.find()) {
					sentences[count] += matcher.group();
				}
				count++;
			}
		}
		return sentences;
	}

}
