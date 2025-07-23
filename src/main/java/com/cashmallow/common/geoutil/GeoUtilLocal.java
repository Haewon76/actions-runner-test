package com.cashmallow.common.geoutil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!dev & !prd")
public class GeoUtilLocal implements GeoUtil {

    @PostConstruct
    @Override
    public void init() throws IOException {
        // Do nothing
        File file = new File("IP2LOCATION-LITE-DB11.IPV6.BIN");
        System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
        if (file.exists()) {
            loc.Open(file.getAbsolutePath(), true);
        }
    }

}