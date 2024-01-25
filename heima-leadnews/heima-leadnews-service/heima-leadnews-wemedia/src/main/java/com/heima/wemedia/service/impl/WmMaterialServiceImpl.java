package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class WmMaterialServiceImpl implements WmMaterialService {
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 上传图片素材
     * @param multipartFile
     * @return
     */
    @Override
    @Transactional
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        //1、校验参数
        if(multipartFile==null||multipartFile.getSize()==0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、上传图片到minio
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //源文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //后缀名
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //图片minio路径
        String filePath=null;
        try {
            filePath = fileStorageService.uploadImgFile("", uuid + postfix, multipartFile.getInputStream());
            log.info("上传图片成功，filePath：{}",filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("WmMaterialServiceImpl上传图片失败");
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"图片上传失败");
        }
        //3、保存图片素材数据
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtils.getUser().getId());
        wmMaterial.setUrl(filePath);
        wmMaterial.setIsCollection((short) 0);//默认不收藏
        wmMaterial.setType((short) 0);//0 图片、1 视频
        wmMaterial.setCreatedTime(new Date());
        int insert = wmMaterialMapper.insert(wmMaterial);
        if(insert<=0){
            log.info("保存图片到数据库失败");
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"图片保存失败");
        }
        //4、返回图片访问地址
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 查询图片列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(WmMaterialDto dto){
        //1、校验参数
        dto.checkParam();
        //2、分页查询
        Page<WmMaterial> page=new Page<>(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmMaterial> queryWrapper = new LambdaQueryWrapper<WmMaterial>()
                .eq(WmMaterial::getUserId, WmThreadLocalUtils.getUser().getId())  //按照用户查询
                .orderByDesc(WmMaterial::getCreatedTime);  //按时间降序
        //是否收藏
        if(dto.getIsCollection()!=null&&dto.getIsCollection()==1){
            queryWrapper.eq(WmMaterial::getIsCollection,dto.getIsCollection());
        }
        Page<WmMaterial> result = wmMaterialMapper.selectPage(page, queryWrapper);
        //3、封装返回结果
        ResponseResult responseResult=new PageResponseResult(dto.getPage(),dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
    }

    /**
     * 删除图片
     * @param id 图片素材id
     * @return
     */
    @Override
    @Transactional
    public ResponseResult delPicture(Integer id) {
        //1、校验参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询数据
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3、从minio中删除数据
        String url = wmMaterial.getUrl();
        boolean b = fileStorageService.delete(url);
        if(!b){
            log.info("从minio中删除文件失败,id:{}",id);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文件删除失败");
        }
        //4、删除数据库中数据
        int i = wmMaterialMapper.deleteById(wmMaterial);
        if(i<=0){
            log.info("从数据库删除文件失败,id:{}",id);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文件删除失败");
        }
        return ResponseResult.okResult(null);
    }

    /**
     * 取消或添加收藏
     * @param id   图片素材id
     * @param type 1收藏 0取消收藏
     * @return
     */
    @Override
    @Transactional
    public ResponseResult change_collect(Integer id, Short type) {
        //1、校验参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询数据
        WmMaterial wmMaterial = wmMaterialMapper.selectById(id);
        if(wmMaterial==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3、修改数据并保存
        wmMaterial.setIsCollection(type);
        int i = wmMaterialMapper.updateById(wmMaterial);
        if(i<=0){
            log.info("取消或添加收藏,id:{}",id);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
        return ResponseResult.okResult(null);
    }
}
