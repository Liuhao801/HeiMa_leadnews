package com.heima.wemedia.service;

import com.heima.model.wemedia.dtos.WmNewsAuthDto;
import com.heima.model.wemedia.vos.WmNewsVo;
import com.heima.wemedia.mapper.WmNewsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class WmNewsTest {

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Test
    public void wnNewsTest(){
        WmNewsAuthDto dto =new WmNewsAuthDto();
        dto.setPage(1);
        dto.setSize(10);
        List<WmNewsVo> listAndPage = wmNewsMapper.findListAndPage(dto);
        for (WmNewsVo wmNewsVo : listAndPage) {
            System.out.println("wmNewsVo = " + wmNewsVo);
        }
    }
}
