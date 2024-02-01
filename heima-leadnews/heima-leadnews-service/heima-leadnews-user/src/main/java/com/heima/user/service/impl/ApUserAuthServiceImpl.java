package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.api.wemedia.IWemediaClient;
import com.heima.common.constants.UserConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.ApUserAuthDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.signature.qual.SignatureUnknown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class ApUserAuthServiceImpl implements ApUserAuthService {
    @Autowired
    private ApUserRealnameMapper apUserRealnameMapper;
    @Autowired
    private ApUserMapper apUserMapper;
    @Autowired
    private IWemediaClient wemediaClient;

    /**
     * 分页查询用户认证信息
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(ApUserAuthDto dto) {
        //1、检查参数
        dto.checkParam();
        //2、设置分页参数
        Page<ApUserRealname> page = new Page<>(dto.getPage(),dto.getSize());
        //3、设置查询条件
        LambdaQueryWrapper<ApUserRealname> queryWrapper = new LambdaQueryWrapper<ApUserRealname>()
                .eq(dto.getStatus() != null, ApUserRealname::getStatus, dto.getStatus())  //根据审核状态查询
                .orderByDesc(ApUserRealname::getCreatedTime);  //根据创建时间降序
        Page<ApUserRealname> result = apUserRealnameMapper.selectPage(page, queryWrapper);
        //4、封装返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
    }

    /**
     * 修改审核状态
     * @param dto
     * @param status
     * @return
     */
    @Override
    @Transactional
    public ResponseResult updateStatus(ApUserAuthDto dto,Short status) {
        //1、检查参数
        if(dto==null||dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、根据用户id查询用户
        ApUserRealname apUserRealname = apUserRealnameMapper.selectById(dto.getId());
        if(apUserRealname==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
        }
        //3、查询用户状态
        if(!apUserRealname.getStatus().equals(ApUserRealname.Status.SUBMIT.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"只有待审核的用户可以修改审核状态");
        }
        //4、修改用户状态
        apUserRealname.setStatus(status);  // 2 审核失败  9 审核成功
        if(StringUtils.isNotBlank(dto.getMsg())){
            apUserRealname.setReason(dto.getMsg());  //拒绝原因
        }
        apUserRealname.setUpdatedTime(new Date());
        int i = apUserRealnameMapper.updateById(apUserRealname);
        if(i<=0){
            log.error("修改用户审核状态失败",dto.getId());
        }

        //5.如果审核状态是9，就是成功，需要创建自媒体账户
        if(status.equals(UserConstants.PASS_AUTH)){
            ResponseResult responseResult = createWmUserAndAuthor(dto);
            if(responseResult != null){
                return responseResult;
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 创建自媒体账户
     * @param dto
     * @return
     */
    private ResponseResult createWmUserAndAuthor(ApUserAuthDto dto) {
        //查询用户认证信息
        ApUserRealname apUserRealname = apUserRealnameMapper.selectById(dto.getId());
        if(apUserRealname == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //查询app端用户信息
        ApUser apUser = apUserMapper.selectById(apUserRealname.getUserId());
        if(apUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //创建自媒体账户
        WmUser wmUser = wemediaClient.findWmUserByName(apUser.getName());
        if(wmUser == null){
            wmUser= new WmUser();
            wmUser.setApUserId(apUser.getId());
            wmUser.setCreatedTime(new Date());
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setPhone(apUser.getPhone());
            wmUser.setStatus(9);
            wemediaClient.saveWmUser(wmUser);
        }
        apUser.setFlag((short)1);
        apUser.setIdentityAuthentication(true);
        int i = apUserMapper.updateById(apUser);
        if(i<=0){
            log.error("修改用户表失败，用户id:{}",apUser.getId());
        }
        return null;
    }

}
