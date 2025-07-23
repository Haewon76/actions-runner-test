package com.cashmallow;

import com.cashmallow.common.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@EnableFeignClients
@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
@EnableRetry
@EnableAsync
@EnableRedisHttpSession(redisNamespace = "cashmallow-api", maxInactiveIntervalInSeconds = 3600)
public class Application {

    @Value("${mail.host}")
    private String mailHost;
    @Value("${mail.port}")
    private String mailPort;
    @Value("${paygate.url}")
    private String paygateUrl;
    @Value("${scb.baseUrl}")
    private String scbBaseUrl;
    @Value("${rabbitmq.host}")
    private String rabbitmqHost;
    @Value("${datasource.master.hikari.jdbc-url}")
    private String datasourceMaster;
    @Value("${datasource.slave.hikari.jdbc-url}")
    private String datasourceSlave;
    @Autowired
    private EnvUtil envUtil;

    @Value("${host.file.path.home}")
    private String hostFilePathHome;

    public static void main(String[] args) {

        String port = null;
        String filename = null;

        for (String arg : args) {
            if (arg.contains("server.port=")) {
                port = arg.substring("server.port=".length() + 2);
            }
        }

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

        filename = String.format("./bin/shutdown.%s.%s.pid", port, df.format(new Date()));

        SpringApplicationBuilder app = new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET);
        app.build().addListeners(new ApplicationPidFileWriter(filename));
        app.run(args);
    }

    @PostConstruct
    public void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        showBanner();

        Arrays.asList("AUTHME", "ADDRESS", "BANKBOOK", "BIZ", "CERTIFICATION", "CMRECEIPT", "PASSPORT", "PROFILE", "RECEIPT", "RECEIPT_REMIT", "SHOP", "tmp").forEach(f -> {
            String path = hostFilePathHome + "/" + f;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        });
    }

    // 배너 환경 변수 설정
    private void showBanner() {
        String sb = "◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤" + "\n" +
                ",---.          |              |    |" + "\n" +
                "|    ,---.,---.|---.,-.-.,---.|    |    ,---.. . ." + "\n" +
                "|    ,---|`---.|   || | |,---||    |    |   || | |" + "\n" +
                "`---'`---^`---'`   '` ' '`---^`---'`---'`---'`-'-'" + "\n" +
                String.format("cashmallow-api(%s)", envUtil.getEnv()) + "\n" +
                String.format("smtp : %s(%s)", mailHost, mailPort) + "\n" +
                String.format("spring.rabbitmq.host: %s", rabbitmqHost) + "\n" +
                String.format("db-1: %s", datasourceMaster) + "\n" +
                String.format("db-2: %s", datasourceSlave) + "\n" +
                "living for today!" + "\n" +
                "◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤◢◣◥◤" + "\n";
        log.info(sb);
    }
}
