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
@Profile({"prd", "dev"})
public class GeoUtilRemote implements GeoUtil {

    @PostConstruct
    @Override
    public void init() throws IOException {
        File file = new File("/cm-spring-data/IP2LOCATION-LITE-DB11.IPV6.BIN");
        if (!file.exists()) {
            throw new IOException("File not found");
        }

        loc.Open(file.getAbsolutePath(), true);
    }

}