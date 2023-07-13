package com.cnpc.epai;

import com.cnpc.epai.core.workscene.entity.Keyword;
import com.cnpc.epai.core.workscene.mapper.KeywordMapper;
import com.cnpc.epai.core.workscene.service.TreeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StartApplicationTests {

	@Autowired
	private KeywordMapper keywordMapper;

    @Test
	public void contextLoads() {

	}

}
