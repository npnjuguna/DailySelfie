package com.njuguna.dailyselfie.server.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = com.njuguna.dailyselfie.server.ServerApplication.class)
@WebAppConfiguration
public class ServerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
