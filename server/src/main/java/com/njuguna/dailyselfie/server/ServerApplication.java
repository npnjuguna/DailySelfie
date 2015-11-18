package com.njuguna.dailyselfie.server;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCouchbaseRepositories
public class ServerApplication extends AbstractCouchbaseConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    protected List<String> getBootstrapHosts() {
        return Arrays.asList(Config.COUCHBASE_SERVER_1);
    }

    @Override
    protected String getBucketName() {
        return Config.COUCHBASE_SERVER_BUCKET_NAME;
    }

    @Override
    protected CouchbaseEnvironment getEnvironment() {
        return DefaultCouchbaseEnvironment.builder()
                .connectTimeout(TimeUnit.SECONDS.toMillis(120))
                .computationPoolSize(6)
                .build();
    }

    @Override
    protected String getBucketPassword() {
        return Config.COUCHBASE_SERVER_BUCKET_PASSWORD;
    }
}
