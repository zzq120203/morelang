package cn.ac.iie.ProcessingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.labs.langid.LangIdV3;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;

import cn.ac.iie.entity.WXMessOriginEntity;

public class LanguageFilterHandler implements EventHandler<WXMessOriginEntity>,LifecycleAware  {

	private static Logger LOG = LoggerFactory.getLogger(LanguageFilterHandler.class);
	
	private static final String REGEX = "[\\pP\\p{Punct}\\pC\\pZ\\pS\\pN\t]";
	
	private LangIdV3 lang;
	
    private String oldName;
    private final String name="LanguageFilterHandlerThread";
	
	public LanguageFilterHandler() {
		super();
		lang = new LangIdV3();
	}

	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0)
				return;
			if (event.getM_type() == 1) {
				String langCode = getLangID(event.getM_content());
//				LOG.debug(event.getM_content() + "  langCode:" + langCode);
				event.setM_language(langCode);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	
	private String getLangID(String content) throws Exception{
		String langid = null;
		
		String tmpContent = content.replaceAll(REGEX, "");
//		System.out.println("["+tmpContent+"]");
		if (tmpContent.isEmpty()) {
			return langid;
		}
		
		String langCode = lang.classify(tmpContent, true).langCode;
		switch (langCode) {
		case "zh"://中文
			langid = "Zh";
			break;
		case "en"://英文
			langid = "En";
			break;
		case "dz"://藏文
			langid = "Tibetan";
			break;
		case "ug"://维文
			langid = "Uighur";
			break;
		case "ka"://蒙文
			langid = "Mongolian";
			break;
		case "ja"://日文
			langid = "Ja";
			break;
		case "ko"://韩文
			langid = "Ko";
			break;
		default:
//			LOG.debug(content + " " + langCode);
			break;
		}
		return langid;
	}
	
	@Override
	public void onStart() {
        final Thread currentThread = Thread.currentThread();
        oldName = currentThread.getName();
        currentThread.setName(name);
	}

	@Override
	public void onShutdown() {
		Thread.currentThread().setName(oldName);
	}

}
