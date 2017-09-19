package cn.ac.iie.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import cn.ac.iie.Confguration.CassandraConnConfig;
import cn.ac.iie.Confguration.ConfLoading;
import cn.ac.iie.Confguration.Config;
import cn.ac.iie.DataExchange.CustomizedMQConsumer;
import cn.ac.iie.DataExchange.MPPDataLoading;
import cn.ac.iie.DataExchange.MQConsumerWrapper;
import cn.ac.iie.DataExchange.MessageListenerConcurrentlyImp;
import cn.ac.iie.MetaData.LanguageWhiteListMaintaining;
import cn.ac.iie.MetaData.LocaleWhiteListMaintaining;
import cn.ac.iie.MetaData.SurveilChatroomInfoMaintaining;
import cn.ac.iie.MetaData.SurveilKeywordInfoMaintaining;
import cn.ac.iie.MetaData.SurveilLocaleInfoMaintaining;
import cn.ac.iie.MetaData.SurveilZHInfoMaintaining;
import cn.ac.iie.MetaData.URLKeywordWhiteListInfoMaintaining;
import cn.ac.iie.ProcessingHandler.*;
import cn.ac.iie.Util.GroupGather;
import cn.ac.iie.Util.LargeBitmap;
import cn.ac.iie.Util.RPoolProxy;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.REConnectionException;
import cn.ac.iie.entity.URLInfoEntity;
import cn.ac.iie.entity.WXMessOriginEntity;

public class MoreLanguage {

	private final static Logger LOG = LoggerFactory.getLogger(MoreLanguage.class);

	public static Disruptor<WXMessOriginEntity> disruptor = null;
	public static Disruptor<URLInfoEntity> disruptor_url = null;

	public static LinkedList<RPoolProxy> rppList = new LinkedList<RPoolProxy>();

	public static LinkedList<LargeBitmap> lbList = new LinkedList<LargeBitmap>();

	public static LinkedList<GroupGather> ggList = new LinkedList<GroupGather>();

	private static void initFromConifgDB() {
		Executors.newSingleThreadExecutor().submit(new SurveilLocaleInfoMaintaining());
		Executors.newSingleThreadExecutor().submit(new LanguageWhiteListMaintaining());
		Executors.newSingleThreadExecutor().submit(new LocaleWhiteListMaintaining());
		Executors.newSingleThreadExecutor().submit(new URLKeywordWhiteListInfoMaintaining());
		Executors.newSingleThreadExecutor().submit(new SurveilChatroomInfoMaintaining());
		Executors.newSingleThreadExecutor().submit(new SurveilZHInfoMaintaining());
		Executors.newSingleThreadExecutor().submit(new SurveilKeywordInfoMaintaining());
		while ((SurveilKeywordInfoMaintaining.isInit()
				&& SurveilZHInfoMaintaining.isInit()
				&& SurveilKeywordInfoMaintaining.isInit()
				&& URLKeywordWhiteListInfoMaintaining.isInit()
				&& LanguageWhiteListMaintaining.isInit()
				&& LocaleWhiteListMaintaining.isInit()
				&& SurveilLocaleInfoMaintaining.isInit()
		) == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
			}
		}
	}

	public static Object consumer = null;

	public static boolean fetchFromTestMQ = false;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException,
			IllegalArgumentException, IllegalAccessException, IOException {
		
		if (args.length == 1 && args[0].toLowerCase().equals("help")) {
			ConfLoading.helpInfo(Config.class);
			return;
		}
		PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
		
		LOG.info("Procedure starting....");
		ConfLoading.init(Config.class, System.getProperty("config"));
//		ConfLoading.init(Config.class, "configs/config.txt");
		
		initFromConifgDB();

		CassandraConnConfig allMessConf=new CassandraConnConfig();
		allMessConf.setConnAddress(Config.ConnAddressAllMess);
		allMessConf.setDataCenter(Config.DataCenterAllMess);
		allMessConf.setPort(Config.MppPortAllMess);
		allMessConf.setUserName(Config.UserNameAllMess);
		allMessConf.setPassword(Config.PasswordAllMess);
		allMessConf.setKeySpace(Config.KeySpaceAllMess);
		
		CassandraConnConfig otherMessConf=new CassandraConnConfig();
		otherMessConf.setConnAddress(Config.ConnAddressOther);
		otherMessConf.setDataCenter(Config.DataCenterOther);
		otherMessConf.setPort(Config.MppPortOther);
		otherMessConf.setUserName(Config.UserNameOther);
		otherMessConf.setPassword(Config.PasswordOther);
		otherMessConf.setKeySpace(Config.KeySpaceOther);
		
		MPPDataLoading.init(allMessConf, otherMessConf);

		
		EventFactory<URLInfoEntity> RAW_FACTORY_URL = new EventFactory<URLInfoEntity>(){
			@Override
			public URLInfoEntity newInstance() {
				return new URLInfoEntity();
			}
		};
		
		URLPubishHandler[] urlPubishHandler = new URLPubishHandler[Config.threadNumber];
		for (int i = 0; i < urlPubishHandler.length; i++) {
			urlPubishHandler[i] = new URLPubishHandler(Config.StoreMQAddress,Config.StoreMQPort,
					Config.UrlStoreConsumerGroup,Config.UrlTopic);
		}
		
		disruptor_url = new Disruptor<URLInfoEntity>(
				RAW_FACTORY_URL, 256, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new BlockingWaitStrategy());
		disruptor_url.handleEventsWithWorkerPool(urlPubishHandler);
//		disruptor_url.handleEventsWith(new URLPubishHandler(Config.StoreMQAddress,Config.StoreMQPort,
//				Config.UrlStoreConsumerGroup,Config.UrlTopic));
		RingBuffer<URLInfoEntity> url_ringBuffer = disruptor_url.start();
		
		
		EventFactory<WXMessOriginEntity> RAW_FACTORY_ORI_MESS = new EventFactory<WXMessOriginEntity>() {
			@Override
			public WXMessOriginEntity newInstance() {
				return new WXMessOriginEntity();
			}
		};


		// revise later for high performance
		disruptor = new Disruptor<WXMessOriginEntity>(RAW_FACTORY_ORI_MESS, 256, DaemonThreadFactory.INSTANCE,
				ProducerType.MULTI, new BlockingWaitStrategy());

		
		disruptor//.handleEventsWith((x,y,z)->{if(x.getM_type()==3)System.out.println("type:"+x.getM_type()+" mmurl:"+x.getM_mm_url());})
				.handleEventsWith(new LocaleFilterHandler(Config.ip2regiondb))// 区域识别
				.then(new HotEventHandler())// 热点聚类
				.then(new LanguageFilterHandler())// 语种识别
				//.then(new URLFilterHandler(url_ringBuffer))// url推送
				.then(new DataPushHandler())//多语种数据推送
				//.then(new ChatRoomFilteringHandler())//重点群
				//.then(new WXIDFilteringHandler())//重点用户
				//.then(new ContentAnalysisHandler())// 内容分析,url
				//.then(new DataPersistenceHandler())// 入库
		;

		RingBuffer<WXMessOriginEntity> ringBuffer = disruptor.start();

		try {
			consumer = new CustomizedMQConsumer(Config.OriMessMQAddress, Config.OriMessMQPort,
					Config.OriMessConsumerGroup, Config.OriMessTopic, ringBuffer, Config.OriMessConsumerthreadNr);
			((CustomizedMQConsumer) consumer).startConsumer();
			Thread thread = new Thread((CustomizedMQConsumer) consumer, "PeriodBeatingThread");
			thread.start();
		} catch (REConnectionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				LOG.info("Execute shutdown Hook.....");
				handle();
			}

			public void handle() {
				if (MoreLanguage.consumer instanceof MQConsumerWrapper) {
					((MQConsumerWrapper) MoreLanguage.consumer).stopConsumer();
					MessageListenerConcurrentlyImp.stopSignalSend.set(true);
				} else if (MoreLanguage.consumer instanceof CustomizedMQConsumer) {
					((CustomizedMQConsumer) MoreLanguage.consumer).stopConsumer();
					CustomizedMQConsumer.stopSignalSend.set(true);
				}

				MoreLanguage.disruptor.shutdown();

				for (RPoolProxy rpp : rppList) {
					rpp.quit();
				}
				for (LargeBitmap lb : lbList) {
					lb.closeAll();
				}

				for (GroupGather gg : ggList) {
					gg.closeAll();
				}

				LOG.info("Program exiting......");
			}
		}));
	}
}
