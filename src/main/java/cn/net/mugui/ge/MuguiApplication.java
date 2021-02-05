package cn.net.mugui.ge;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.mugui.spring.base.ModelManagerInterface;
import com.mugui.spring.net.baghandle.NetBagModuleManager;

@ComponentScan(basePackages = { "cn.net.mugui.*","com.mugui.*", "com.hx.*"})
@SpringBootApplication
@EnableScheduling
@org.apache.dubbo.config.spring.context.annotation.EnableDubbo(scanBasePackages = {"cn.net.mugui.*","com.mugui.*", "com.hx.*"})
public class MuguiApplication extends WebMvcConfigurationSupport {

	public static String APPLICATION_PATH = null;
	static {
		System.setProperty("sun.jnu.encoding", "utf-8");
		APPLICATION_PATH = MuguiApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		try {
			APPLICATION_PATH = URLDecoder.decode(new File(APPLICATION_PATH).getParent(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (!new File(APPLICATION_PATH).isDirectory()) {
			ApplicationHome home = new ApplicationHome();
			APPLICATION_PATH = home.getSource().getParentFile().getAbsolutePath();
		}

	}

	public static ApplicationContext run(String[] args) throws Exception {
		ApplicationContext app = SpringApplication.run(MuguiApplication.class, args);
		System.getProperties().put("Application", app);
		{
			//以下为修复阿里服务加载bug
			Environment bean =(Environment) app.getBean("environment");
			String property = bean.getProperty("dubbo.registry.address");
			if(StringUtils.isNotBlank(property)) {
				System.setProperty("dubbo.registry.address", property);
			}
		}
		LogInit(args);
		app.getBean(NetBagModuleManager.class).invokeFunction("init", MuguiApplication.class);
		return app;
	}

//	@Bean
//	public ServerEndpointExporter serverEndpointExporter() {
//		return new ServerEndpointExporter();
//	}

	/**
	 * 日志系统初始化
	 * 
	 * @param args
	 */
	private static void LogInit(String[] args) {
		PrintStream out = new PrintStream(System.out) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

			@Override
			public void print(String s) {
				super.print(
						format.format(new Date()) + ":\t" + "Thread:" + Thread.currentThread().getName() + "    :" + s);
			}

			@Override
			public void println(String x) {
				print(x + "\r\n");
			}

			@Override
			public void print(Object obj) {
				print(String.valueOf(obj));
			}

			@Override
			public void println(Object x) {
				println(String.valueOf(x));
			}

			@Override
			public void print(char c) {
				print(String.valueOf(c));
			}

			@Override
			public void print(boolean b) {
				print(String.valueOf(b));
			}

			@Override
			public void print(float f) {
				print(String.valueOf(f));
			}

			@Override
			public void print(int i) {
				print(String.valueOf(i));
			}

			@Override
			public void println(char c) {
				println(String.valueOf(c));
			}

			@Override
			public void println(boolean b) {
				println(String.valueOf(b));
			}

			@Override
			public void println(float f) {
				println(String.valueOf(f));
			}

			@Override
			public void println(int i) {
				println(String.valueOf(i));
			}
		};
		System.setOut(out);
	}

	@Override
	protected void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
		configurer.setDefaultTimeout(20000L);
		configurer.registerCallableInterceptors(timeoutInterceptor());
		configurer.setTaskExecutor(threadPoolTaskExecutor());

	}

	@Bean
	public TimeoutCallableProcessingInterceptor timeoutInterceptor() {
		return new TimeoutCallableProcessingInterceptor();
	}

	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
		t.setCorePoolSize(10);
		t.setMaxPoolSize(512);
		t.setThreadNamePrefix("MUGUI");
		return t;
	}
}
